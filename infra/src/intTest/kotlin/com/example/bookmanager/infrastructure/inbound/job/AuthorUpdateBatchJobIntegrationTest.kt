package com.example.bookmanager.infrastructure.inbound.job

import com.example.bookmanager.application.ApplicationException
import com.example.bookmanager.application.port.inbound.RunAuthorUpdateBatchJobInputPort.UpdateTaskData
import com.example.bookmanager.application.port.inbound.SearchAuthorInputPort
import com.example.bookmanager.application.port.inbound.UpdateAuthorAffiliationInputPort
import com.example.bookmanager.application.port.outbound.IdempotencyRepository
import com.example.bookmanager.application.port.outbound.MessagePoller
import com.example.bookmanager.application.port.outbound.PolledMessage
import com.example.bookmanager.application.port.outbound.RemoteStorage
import com.example.bookmanager.application.port.outbound.TaskNotifier
import com.example.bookmanager.application.usecase.RunAuthorUpdateBatchJobUseCase
import com.example.bookmanager.infrastructure.outbound.messaging.SfnTaskNotifier
import com.example.bookmanager.infrastructure.outbound.messaging.SqsMessagePoller
import com.example.bookmanager.support.author.AuthorFixture
import com.example.bookmanager.support.author.insert
import com.example.bookmanager.support.db.IntegrationTestSupport
import com.fasterxml.jackson.databind.ObjectMapper
import org.jooq.impl.DSL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sfn.model.SendTaskFailureRequest
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [AuthorUpdateBatchJobIntegrationTest.AwsInitializer::class])
class AuthorUpdateBatchJobIntegrationTest : IntegrationTestSupport() {
    @Autowired private lateinit var sqsClient: SqsClient

    @Autowired private lateinit var s3Client: S3Client

    @Autowired private lateinit var objectMapper: ObjectMapper

    @Autowired private lateinit var searchAuthor: SearchAuthorInputPort

    @Autowired private lateinit var updateAuthor: UpdateAuthorAffiliationInputPort

    @Autowired private lateinit var idempotencyRepository: IdempotencyRepository

    @Autowired private lateinit var storage: RemoteStorage

    @Test
    fun `SQSの1メッセージをS3のJSONLとして読み既存の著者所属更新ユースケースで更新する`() {
        val author = AuthorFixture.natsume()
        dsl.insert(author)
        val inputKey = "inputs/${System.nanoTime()}.jsonl"
        val inputPath = "s3://$BUCKET/$inputKey"
        val outputDirectory = "s3://$BUCKET/results"
        val taskId = UUID.randomUUID()
        val idempotencyEntryCount = dsl.fetchCount(DSL.table("idempotency_entries"))
        s3Client.putObject(
            PutObjectRequest
                .builder()
                .bucket(BUCKET)
                .key(inputKey)
                .build(),
            RequestBody.fromString("""{"authorId":"${author.id}","affiliation":"新しい出版社"}"""),
        )
        sendMessage(
            """{"taskToken":"task-token","data":{"id":"$taskId","inputFilePath":"$inputPath","outputDirectoryPath":"$outputDirectory"}}""",
        )

        val sfn = mock<SfnClient>()
        job(SfnTaskNotifier(sfn)).execute()

        val updated = searchAuthor.execute(author.id, null).single()
        assertEquals("新しい出版社", updated.affiliation)
        assertEquals(author.name, updated.name)
        assertEquals(author.birthDate, updated.birthDate)
        assertEquals(idempotencyEntryCount + 1, dsl.fetchCount(DSL.table("idempotency_entries")))
        verify(sfn).sendTaskSuccess(
            SendTaskSuccessRequest
                .builder()
                .taskToken("task-token")
                .output("{}")
                .build(),
        )
        val output =
            s3Client
                .getObjectAsBytes(
                    GetObjectRequest
                        .builder()
                        .bucket(BUCKET)
                        .key(outputKey(taskId))
                        .build(),
                ).asString(StandardCharsets.UTF_8)
        val result = objectMapper.readTree(output)
        assertEquals(true, result.get("updated").asBoolean())
        assertEquals(1, result.get("lineNumber").asInt())
        assertEquals(author.id.toString(), result.get("authorId").asText())
    }

    @Test
    fun `途中の不正な所属で失敗通知し先行行の更新を保持して冪等性ロックを解放する`() {
        val author = AuthorFixture.natsume()
        dsl.insert(author)
        val taskId = UUID.randomUUID()
        val inputKey = "inputs/$taskId.jsonl"
        s3Client.putObject(
            PutObjectRequest
                .builder()
                .bucket(BUCKET)
                .key(inputKey)
                .build(),
            RequestBody.fromString(
                """{"authorId":"${author.id}","affiliation":"先行行の出版社"}
                    |{"authorId":"${author.id}","affiliation":"${"あ".repeat(256)}"}
                """.trimMargin(),
            ),
        )
        val messageId = UUID.randomUUID().toString()
        val poller = fixedPoller(messageId, UpdateTaskData(taskId, "s3://$BUCKET/$inputKey", "s3://$BUCKET/results"))
        val sfn = mock<SfnClient>()
        val exception = assertThrows<ApplicationException> { job(SfnTaskNotifier(sfn), poller).execute() }
        assertEquals("先行行の出版社", searchAuthor.execute(author.id, null).single().affiliation)
        verify(sfn).sendTaskFailure(
            SendTaskFailureRequest
                .builder()
                .taskToken("task-token")
                .error("TaskFailed")
                .cause(exception.message)
                .build(),
        )
        assertEquals(true, idempotencyRepository.tryStart("${UpdateTaskData.TASK_TYPE.queueName}:$messageId"))
        idempotencyRepository.release("${UpdateTaskData.TASK_TYPE.queueName}:$messageId")
        assertThrows<software.amazon.awssdk.services.s3.model.NoSuchKeyException> {
            s3Client.getObjectAsBytes(
                GetObjectRequest
                    .builder()
                    .bucket(BUCKET)
                    .key(outputKey(taskId))
                    .build(),
            )
        }
    }

    @Test
    fun `空文字で所属をクリアし完了済みメッセージは再処理しない`() {
        val author = AuthorFixture.natsume()
        dsl.insert(author)
        updateAuthor.execute(
            com.example.bookmanager.application.port.inbound
                .UpdateAuthorAffiliationCommand(author.id, "出版社"),
        )
        val taskId = UUID.randomUUID()
        val inputKey = "inputs/$taskId.jsonl"
        s3Client.putObject(
            PutObjectRequest
                .builder()
                .bucket(BUCKET)
                .key(inputKey)
                .build(),
            RequestBody.fromString("""{"authorId":"${author.id}","affiliation":""}"""),
        )
        val poller = fixedPoller(UUID.randomUUID().toString(), UpdateTaskData(taskId, "s3://$BUCKET/$inputKey", "s3://$BUCKET/results/"))
        job(mock<TaskNotifier>(), poller).execute()
        assertEquals("", searchAuthor.execute(author.id, null).single().affiliation)
        updateAuthor.execute(
            com.example.bookmanager.application.port.inbound
                .UpdateAuthorAffiliationCommand(author.id, "別の出版社"),
        )
        val notifier = mock<TaskNotifier>()
        job(notifier, poller).execute()
        assertEquals("別の出版社", searchAuthor.execute(author.id, null).single().affiliation)
        verifyNoInteractions(notifier)
    }

    @Test
    fun `存在しない著者は更新できない`() {
        assertThrows<ApplicationException> {
            updateAuthor.execute(
                com.example.bookmanager.application.port.inbound.UpdateAuthorAffiliationCommand(
                    com.example.bookmanager.domain.AuthorId
                        .generate()
                        .value,
                    "出版社",
                ),
            )
        }
    }

    private fun fixedPoller(
        messageId: String,
        data: UpdateTaskData,
    ): MessagePoller =
        object : MessagePoller {
            override fun <T : Any> poll(
                queueName: String,
                dataType: Class<T>,
                consumer: (PolledMessage<T>) -> Unit,
            ) {
                assertEquals(UpdateTaskData.TASK_TYPE.queueName, queueName)
                consumer(PolledMessage(messageId, "task-token", dataType.cast(data)))
            }
        }

    private fun job(
        taskNotifier: TaskNotifier,
        poller: MessagePoller = SqsMessagePoller(sqsClient, objectMapper),
    ) = RunAuthorUpdateBatchJobUseCase(
        poller,
        idempotencyRepository,
        taskNotifier,
        updateAuthor,
        storage,
    )

    private fun sendMessage(body: String) {
        val queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(UpdateTaskData.TASK_TYPE.queueName).build()).queueUrl()
        sqsClient.sendMessage(
            SendMessageRequest
                .builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build(),
        )
    }

    private fun outputKey(taskId: UUID): String = "results/author-update-result-$taskId.jsonl"

    class AwsInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            SqsClient
                .builder()
                .endpointOverride(URI.create(endpoint()))
                .region(Region.of(region()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build()
                .use {
                    it.createQueue(CreateQueueRequest.builder().queueName(UpdateTaskData.TASK_TYPE.queueName).build())
                }
            S3Client
                .builder()
                .endpointOverride(URI.create(endpoint()))
                .region(Region.of(region()))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build()
                .use {
                    runCatching { it.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build()) }
                }
        }
    }

    companion object {
        private const val BUCKET = "author-update-batch-integration"

        private fun endpoint() = System.getenv("SPRING_CLOUD_AWS_ENDPOINT") ?: "http://localhost:4566"

        private fun region() = System.getenv("SPRING_CLOUD_AWS_REGION_STATIC") ?: "ap-northeast-1"
    }
}
