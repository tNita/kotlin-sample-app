package com.example.bookmanager.infrastructure.inbound.messaging

import com.example.bookmanager.application.usecase.SearchAuthorUseCase
import com.example.bookmanager.support.db.IntegrationTestSupport
import org.jooq.impl.DSL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "bookmanager.messaging.author-registration.enabled=true",
    ],
)
@ContextConfiguration(initializers = [AuthorRegistrationSqsEndToEndIntegrationTest.QueueInitializer::class])
class AuthorRegistrationSqsEndToEndIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var sqsAsyncClient: SqsAsyncClient

    @Autowired
    private lateinit var searchAuthorUseCase: SearchAuthorUseCase

    @Test
    fun `SQSメッセージを購読して著者登録し重複メッセージは冪等性管理テーブルで破棄する`() {
        val suffix = System.nanoTime()
        val name = "SQS統合テスト著者-$suffix"
        val birthDate = LocalDate.parse("1896-08-27")
        val body = """{"name":"$name","birthDate":"$birthDate"}"""
        val metadataKey = "author-registration:$name:$birthDate"

        sendMessage(body)

        waitUntil {
            searchAuthorUseCase.exec(id = null, name = name).size == 1 &&
                metadataCount(metadataKey) == 1
        }

        sendMessage(body)

        TimeUnit.SECONDS.sleep(1)
        assertEquals(1, searchAuthorUseCase.exec(id = null, name = name).size)
        assertEquals(1, metadataCount(metadataKey))
    }

    private fun sendMessage(body: String) {
        val queueUrl =
            sqsAsyncClient
                .getQueueUrl(
                    GetQueueUrlRequest
                        .builder()
                        .queueName(QUEUE_NAME)
                        .build(),
                ).get(5, TimeUnit.SECONDS)
                .queueUrl()

        sqsAsyncClient
            .sendMessage(
                SendMessageRequest
                    .builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .build(),
            ).get(5, TimeUnit.SECONDS)
    }

    private fun metadataCount(metadataKey: String): Int =
        dsl.fetchCount(
            DSL.table("int_metadata_store"),
            DSL
                .field("metadata_key", String::class.java)
                .eq(metadataKey)
                .and(DSL.field("region", String::class.java).eq("author-registration")),
        )

    private fun waitUntil(assertion: () -> Boolean) {
        val deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos()
        while (System.nanoTime() < deadline) {
            if (assertion()) {
                return
            }
            TimeUnit.MILLISECONDS.sleep(250)
        }
        throw AssertionError("Timed out waiting for SQS message processing")
    }

    class QueueInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            SqsClient
                .builder()
                .endpointOverride(URI.create(awsEndpoint()))
                .region(Region.of(awsRegion()))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test"),
                    ),
                ).build()
                .use { client ->
                    client.createQueue(
                        CreateQueueRequest
                            .builder()
                            .queueName(QUEUE_NAME)
                            .build(),
                    )
                }
        }
    }

    companion object {
        private const val QUEUE_NAME = "bookmanager-author-registration"

        private fun awsEndpoint(): String = System.getenv("SPRING_CLOUD_AWS_ENDPOINT") ?: "http://localhost:4566"

        private fun awsRegion(): String = System.getenv("SPRING_CLOUD_AWS_REGION_STATIC") ?: "ap-northeast-1"
    }
}
