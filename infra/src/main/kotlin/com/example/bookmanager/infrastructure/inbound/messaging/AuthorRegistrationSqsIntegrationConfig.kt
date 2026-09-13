package com.example.bookmanager.infrastructure.inbound.messaging

import io.awspring.cloud.sqs.integration.SqsMessageDrivenChannelAdapter
import io.awspring.cloud.sqs.listener.QueueNotFoundStrategy
import io.awspring.cloud.sqs.listener.SqsContainerOptions
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.core.MessageProducer
import org.springframework.integration.handler.advice.IdempotentReceiverInterceptor
import org.springframework.integration.jdbc.metadata.JdbcMetadataStore
import org.springframework.integration.selector.MetadataStoreSelector
import org.springframework.messaging.MessageChannel
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.time.Duration
import javax.sql.DataSource

@Configuration
@EnableIntegration
@ConditionalOnProperty(
    prefix = "bookmanager.messaging.author-registration",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class AuthorRegistrationSqsIntegrationConfig(
    @Value("\${bookmanager.messaging.author-registration.queue-name}")
    private val queueName: String,
) {
    @Bean
    fun authorRegistrationInputChannel(): MessageChannel = DirectChannel()

    @Bean
    fun authorRegistrationMetadataStore(dataSource: DataSource): JdbcMetadataStore =
        JdbcMetadataStore(dataSource).apply {
            setRegion("author-registration")
        }

    @Bean
    fun authorRegistrationIdempotentReceiverInterceptor(
        metadataStore: JdbcMetadataStore,
        keyProcessor: AuthorRegistrationIdempotencyKeyProcessor,
    ): IdempotentReceiverInterceptor {
        val selector = MetadataStoreSelector(keyProcessor, metadataStore)

        return IdempotentReceiverInterceptor(selector).apply {
            setDiscardChannelName("nullChannel")
        }
    }

    @Bean
    fun authorRegistrationSqsMessageProducer(sqsAsyncClient: SqsAsyncClient): MessageProducer {
        val adapter = SqsMessageDrivenChannelAdapter(sqsAsyncClient, queueName)
        adapter.setOutputChannelName(AUTHOR_REGISTRATION_INPUT_CHANNEL)
        adapter.setSqsContainerOptions(
            SqsContainerOptions
                .builder()
                .queueNotFoundStrategy(QueueNotFoundStrategy.FAIL)
                .acknowledgementMode(AcknowledgementMode.ON_SUCCESS)
                .maxMessagesPerPoll(10)
                .pollTimeout(Duration.ofSeconds(5))
                .build(),
        )
        return adapter
    }

    companion object {
        const val AUTHOR_REGISTRATION_INPUT_CHANNEL = "authorRegistrationInputChannel"
    }
}
