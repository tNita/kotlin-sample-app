package com.example.bookmanager.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PriceTest {

    @Test
    fun `0以上なら生成される`() {
        val price = Price.of(BigDecimal("1234.56"))
        assertEquals(BigDecimal("1234.56"), price.amount)
    }

    @Test
    fun `負の価格はエラー`() {
        val ex = assertThrows(DomainException::class.java) {
            Price.of(BigDecimal("-1"))
        }
        assertEquals(DomainErrorCode.PRICE_NEGATIVE, ex.code)
        assertEquals("Price must be equal or greater than 0", ex.message)
    }
}
