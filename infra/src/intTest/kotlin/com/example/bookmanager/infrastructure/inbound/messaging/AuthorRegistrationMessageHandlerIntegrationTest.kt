package com.example.bookmanager.infrastructure.inbound.messaging

import com.example.bookmanager.application.usecase.SearchAuthorUseCase
import com.example.bookmanager.support.db.IntegrationTestSupport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthorRegistrationMessageHandlerIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var handler: AuthorRegistrationMessageHandler

    @Autowired
    private lateinit var searchAuthorUseCase: SearchAuthorUseCase

    @Test
    fun `著者登録メッセージを購読して著者を登録できる`() {
        val message =
            AuthorRegistrationMessage(
                name = "宮沢賢治",
                birthDate = LocalDate.parse("1896-08-27"),
            )

        handler.handle(message)

        val authors = searchAuthorUseCase.exec(id = null, name = message.name)
        assertEquals(1, authors.size)
        assertEquals(message.name, authors.single().name)
        assertEquals(message.birthDate, authors.single().birthDate)
    }
}
