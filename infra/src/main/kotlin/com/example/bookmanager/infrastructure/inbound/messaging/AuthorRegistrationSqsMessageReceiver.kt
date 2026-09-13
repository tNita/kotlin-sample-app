package com.example.bookmanager.infrastructure.inbound.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.integration.annotation.IdempotentReceiver
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "bookmanager.messaging.author-registration",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class AuthorRegistrationSqsMessageReceiver(
    private val objectMapper: ObjectMapper,
    private val handler: AuthorRegistrationMessageConsumer,
) {
    @ServiceActivator(inputChannel = AuthorRegistrationSqsIntegrationConfig.AUTHOR_REGISTRATION_INPUT_CHANNEL)
    @IdempotentReceiver("authorRegistrationIdempotentReceiverInterceptor")
    fun receive(payload: String) {
        handler.handle(objectMapper.readValue<AuthorRegistrationMessage>(payload))
    }
}
