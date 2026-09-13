package com.example.bookmanager.infrastructure.inbound.messaging

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class AuthorRegistrationSqsMessageReceiverTest {

    @Test
    fun `SQSメッセージ本文を著者登録メッセージに変換してハンドラへ渡す`() {
        val handler = RecordingAuthorRegistrationMessageConsumer()
        val receiver = AuthorRegistrationSqsMessageReceiver(
            objectMapper = jacksonObjectMapper().findAndRegisterModules(),
            handler = handler,
        )

        receiver.receive("""{"name":"宮沢賢治","birthDate":"1896-08-27"}""")

        assertEquals("宮沢賢治", handler.received.single().name)
        assertEquals(LocalDate.parse("1896-08-27"), handler.received.single().birthDate)
    }

    private class RecordingAuthorRegistrationMessageConsumer : AuthorRegistrationMessageConsumer {
        val received = mutableListOf<AuthorRegistrationMessage>()

        override fun handle(message: AuthorRegistrationMessage) {
            received += message
        }
    }
}
