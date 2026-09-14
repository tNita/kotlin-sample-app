package com.example.bookmanager.application

import com.example.bookmanager.application.port.inbound.RegisterBookCommand
import com.example.bookmanager.application.port.inbound.RegisterBookInputPort
import com.example.bookmanager.application.port.outbound.BookRepository
import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.jooq.tables.BookAuthors.Companion.BOOK_AUTHORS
import com.example.bookmanager.jooq.tables.Books.Companion.BOOKS
import com.example.bookmanager.support.author.AuthorFixture
import com.example.bookmanager.support.author.insert
import com.example.bookmanager.support.db.IntegrationTestSupport
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
@ActiveProfiles("test")
class UseCaseTransactionIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var registerBook: RegisterBookInputPort

    @MockitoSpyBean
    private lateinit var bookRepository: BookRepository

    @Test
    fun `入力ポート経由の書籍登録が保存後に失敗した場合は書籍と著者関連をロールバックする`() {
        val author = AuthorFixture.natsume()
        dsl.insert(author)
        val command =
            RegisterBookCommand(
                title = "こころ",
                price = BigDecimal("1800"),
                authorIds = listOf(AuthorId.generate { author.id }),
            )

        doAnswer { invocation ->
            invocation.callRealMethod()
            throw IllegalStateException("保存後の障害を再現")
        }.`when`(bookRepository).save(any())

        assertFailsWith<IllegalStateException> { registerBook.execute(command) }

        assertEquals(0, dsl.fetchCount(BOOKS))
        assertEquals(0, dsl.fetchCount(BOOK_AUTHORS))
    }

    private fun <T> any(): T = Mockito.any<T>()
}
