package com.example.bookmanager.infrastructure.inbound.job

import com.example.bookmanager.application.port.inbound.GetBookInputPort
import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort.UpdateTaskData
import com.example.bookmanager.application.port.inbound.UpdateBookInputPort
import com.example.bookmanager.application.port.outbound.IdempotencyRepository
import com.example.bookmanager.application.port.outbound.RemoteStorage
import com.example.bookmanager.application.port.outbound.TaskNotified
import com.example.bookmanager.application.port.outbound.TaskNotifier
import com.example.bookmanager.application.usecase.RunBookUpdateBatchJobUseCase
import com.example.bookmanager.infrastructure.outbound.messaging.SqsMessagePoller
import com.example.bookmanager.support.book.seedDefaultBooks
import com.example.bookmanager.support.db.IntegrationTestSupport
import com.fasterxml.jackson.databind.ObjectMapper
import org.jooq.impl.DSL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
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
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [BookUpdateBatchJobIntegrationTest.AwsInitializer::class])
class BookUpdateBatchJobIntegrationTest : IntegrationTestSupport() {
    @Autowired private lateinit var sqsClient: SqsClient

    @Autowired private lateinit var s3Client: S3Client

    @Autowired private lateinit var objectMapper: ObjectMapper

    @Autowired private lateinit var getBook: GetBookInputPort

    @Autowired private lateinit var updateBook: UpdateBookInputPort

    @Autowired private lateinit var idempotencyRepository: IdempotencyRepository

    @Autowired private lateinit var storage: RemoteStorage

    @Test
    fun `SQSの1メッセージをS3のJSONLとして読み既存の書籍更新ユースケースで更新する`() {
        val books = seedDefaultBooks(dsl)
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
            RequestBody.fromString("""{"bookId":"${books.bookId2}","publishStatus":"PUBLISHED","price":2200}"""),
        )
        sendMessage(
            """{"taskToken":"task-token","data":{"id":"$taskId","inputFilePath":"$inputPath","outputDirectoryPath":"$outputDirectory"}}""",
        )

        val taskNotifier = mock<TaskNotifier>()
        job(taskNotifier).execute()

        assertEquals("2200.00", getBook.execute(books.bookId2).price.toPlainString())
        assertEquals("PUBLISHED", getBook.execute(books.bookId2).publishStatus)
        assertEquals(idempotencyEntryCount + 1, dsl.fetchCount(DSL.table("idempotency_entries")))
        verify(taskNotifier).execute(TaskNotified.Success("task-token"))
        val output =
            s3Client
                .getObjectAsBytes(
                    GetObjectRequest
                        .builder()
                        .bucket(BUCKET)
                        .key(outputKey(taskId))
                        .build(),
                ).asString(StandardCharsets.UTF_8)
        assertEquals("true", objectMapper.readTree(output).get("updated").asText())
    }

    private fun job(taskNotifier: TaskNotifier) =
        RunBookUpdateBatchJobUseCase(
            SqsMessagePoller(sqsClient, objectMapper),
            idempotencyRepository,
            taskNotifier,
            getBook,
            updateBook,
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

    private fun outputKey(taskId: UUID): String = "results/book-update-result-$taskId.jsonl"

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
        private const val BUCKET = "book-update-batch-integration"

        private fun endpoint() = System.getenv("SPRING_CLOUD_AWS_ENDPOINT") ?: "http://localhost:4566"

        private fun region() = System.getenv("SPRING_CLOUD_AWS_REGION_STATIC") ?: "ap-northeast-1"
    }
}
