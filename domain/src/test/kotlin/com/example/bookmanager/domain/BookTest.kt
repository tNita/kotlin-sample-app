package com.example.bookmanager.domain

import com.example.bookmanager.shared.Id
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class BookTest {

    private val authorId1 = Id.generate { UUID.fromString("01890a3f-54c1-7f1e-8b10-cbc61ff35f4e") }
    private val authorId2 = Id.generate { UUID.fromString("01890a3f-54c1-7f1e-8b10-cbc61ff35f4f") }

    @Test
    fun `著者が一人もいなければエラー`() {
        val ex = assertThrows(DomainException::class.java) {
            Book.create(
                title = Title.of("Test"),
                price = Price.of(BigDecimal("100")),
                authorIds = emptyList()
            )
        }
        assertEquals(DomainErrorCode.NO_AUTHORS, ex.code)
        assertEquals("Book must have at least one author", ex.message)
    }

    @Test
    fun `著者重複は集合に正規化される`() {
        val book = Book.create(
            title = Title.of("Test"),
            price = Price.of(BigDecimal("1234")),
            authorIds = listOf(authorId1, authorId1, authorId2)
        )

        assertEquals(setOf(authorId1, authorId2), book.authorIds)
    }

    @Test
    fun `出版済みから未出版への変更は例外`() {
        val published = Book.ofExisting(
            id = BookId.of(UUID.fromString("01890a3f-54c1-7f1e-8b10-cbc61ff35f40")),
            title = Title.of("Test"),
            price = Price.of(BigDecimal("1234")),
            publishStatus = PublishStatus.PUBLISHED,
            authorIds = setOf(authorId1)
        )

        val ex = assertThrows(DomainException::class.java) {
            published.withPublishStatus(PublishStatus.UNPUBLISHED)
        }
        assertEquals(DomainErrorCode.INVALID_PUBLISH_STATUS_TRANSITION, ex.code)
        assertEquals("Cannot change publish status from PUBLISHED to UNPUBLISHED", ex.message)
    }
}
