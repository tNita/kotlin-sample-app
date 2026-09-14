package com.example.bookmanager.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class AuthorIdTest {
    @Test
    fun `同じUUIDのIDは集合でも同じ値として扱う`() {
        val original = Id.generate()
        val restored = Id.generate { original.value }

        assertEquals(original, restored)
        assertEquals(1, setOf(original, restored).size)
        assertEquals("Id(value=${original.value})", restored.toString())
    }

    @Test
    fun `生成したIDはUUID v7`() {
        val id = AuthorId.generate()

        assertEquals(7, id.value.version())
    }

    @Test
    fun `著者IDをUUIDから復元できる`() {
        val id = AuthorId.generate()

        assertEquals(id, AuthorId.of(id.value))
    }

    @Test
    fun `書籍IDもUUID v7以外はドメインエラーとして拒否`() {
        val ex = assertThrows(DomainException::class.java) { BookId.of(UUID.randomUUID()) }

        assertEquals(DomainErrorCode.INVALID_ID_VERSION, ex.code)
    }

    @Test
    fun `汎用IDはUUID v7以外を拒否`() {
        val ex = assertThrows(IllegalArgumentException::class.java) { Id.generate { UUID.randomUUID() } }

        assertEquals(IllegalArgumentException::class.java, ex.javaClass)
    }

    @Test
    fun `UUID v7以外は拒否`() {
        val ex =
            assertThrows(DomainException::class.java) {
                AuthorId.generate { UUID.randomUUID() }
            }
        assertEquals(DomainErrorCode.INVALID_ID_VERSION, ex.code)
        assertEquals("ID must be a UUID version 7", ex.message)
    }
}
