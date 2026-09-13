package com.example.bookmanager.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class BirthDateTest {

    @Test
    fun `今日以前の日付は生成される`() {
        val fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC)
        val birthDate = BirthDate.of(LocalDate.parse("2000-05-10"), fixedClock)
        assertEquals(LocalDate.parse("2000-05-10"), birthDate.value)
    }

    @Test
    fun `未来日付はエラー`() {
        val fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC)
        val ex = assertThrows(DomainException::class.java) {
            BirthDate.of(LocalDate.parse("2024-01-02"), fixedClock)
        }
        assertEquals(DomainErrorCode.BIRTHDATE_IN_FUTURE, ex.code)
        assertEquals("Birth date must not be in the future", ex.message)
    }
}
