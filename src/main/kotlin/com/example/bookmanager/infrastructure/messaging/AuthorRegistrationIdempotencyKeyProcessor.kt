package com.example.bookmanager.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.integration.handler.MessageProcessor
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class AuthorRegistrationIdempotencyKeyProcessor(
    private val objectMapper: ObjectMapper,
) : MessageProcessor<String> {
    override fun processMessage(message: Message<*>): String {
        val payload = objectMapper.readValue<AuthorRegistrationMessage>(message.payload as String)
        return "author-registration:${payload.name}:${payload.birthDate}"
    }
}
