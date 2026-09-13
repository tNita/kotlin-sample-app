package com.example.bookmanager.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AuthorAffiliationTest {
    @Test
    fun `空文字と255文字の所属を受け入れ256文字を拒否する`() {
        assertEquals("", AuthorAffiliation.of("").getOrThrow().value)
        assertEquals(
            255,
            AuthorAffiliation
                .of("あ".repeat(255))
                .getOrThrow()
                .value.length,
        )
        val error = AuthorAffiliation.of("あ".repeat(256)).exceptionOrNull() as DomainException
        assertEquals(DomainErrorCode.AUTHOR_AFFILIATION_TOO_LONG, error.code)
    }

    @Test
    fun `所属が異なっても名前と生年月日が同じなら同一人物とみなす`() {
        val name = AuthorName.of("著者")
        val birthDate = BirthDate.of(LocalDate.parse("1990-01-01"))
        val author = Author.create(name, birthDate)
        val other = Author.create(name, birthDate, AuthorAffiliation.of("所属").getOrThrow())
        assertTrue(author.isSamePerson(other))
        assertEquals("", author.affiliation.value)
        assertEquals("所属", other.affiliation.value)
    }
}
