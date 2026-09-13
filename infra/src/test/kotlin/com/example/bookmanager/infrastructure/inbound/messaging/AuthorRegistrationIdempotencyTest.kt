package com.example.bookmanager.infrastructure.inbound.messaging

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.integration.metadata.SimpleMetadataStore
import org.springframework.integration.selector.MetadataStoreSelector
import org.springframework.messaging.support.MessageBuilder
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthorRegistrationIdempotencyTest {

    @Test
    fun `同じ著者登録メッセージはSpring IntegrationのMetadataStoreSelectorで重複扱いになる`() {
        val keyProcessor = AuthorRegistrationIdempotencyKeyProcessor(
            jacksonObjectMapper().findAndRegisterModules(),
        )
        val selector = MetadataStoreSelector(keyProcessor, SimpleMetadataStore())
        val message = MessageBuilder.withPayload("""{"name":"宮沢賢治","birthDate":"1896-08-27"}""")
            .build()

        assertTrue(selector.accept(message))
        assertFalse(selector.accept(message))
    }
}
