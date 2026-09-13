package com.example.bookmanager.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TitleTest {

    @Test
    fun `タイトルが前後空白を除去して生成される`() {
        val title = Title.of("  Kotlin in Action ")
        assertEquals("Kotlin in Action", title.value)
    }

    @Test
    fun `空白のみはエラー`() {
        val ex = assertThrows(DomainException::class.java) {
            Title.of("   ")
        }
        assertEquals(DomainErrorCode.INVALID_TITLE, ex.code)
        assertEquals("Title must not be blank", ex.message)
    }

    @Test
    fun `255文字超はエラー`() {
        val longTitle = "a".repeat(256)
        val ex = assertThrows(DomainException::class.java) {
            Title.of(longTitle)
        }
        assertEquals(DomainErrorCode.TITLE_TOO_LONG, ex.code)
        assertEquals("Title must be 255 characters or less", ex.message)
    }
}
