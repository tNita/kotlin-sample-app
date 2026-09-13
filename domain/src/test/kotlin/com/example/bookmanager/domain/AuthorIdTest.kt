package com.example.bookmanager.domain

import com.example.bookmanager.shared.Id
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class AuthorIdTest {

    @Test
    fun `生成したIDはUUID v7`() {
        val id = Id.generate()

        assertEquals(7, id.value.version())
    }

    @Test
    fun `UUID v7以外は拒否`() {
        val ex = assertThrows(DomainException::class.java) {
            Id.generate { UUID.randomUUID() }
        }
        assertEquals(DomainErrorCode.INVALID_ID_VERSION, ex.code)
        assertEquals("ID must be a UUID version 7", ex.message)
    }
}
