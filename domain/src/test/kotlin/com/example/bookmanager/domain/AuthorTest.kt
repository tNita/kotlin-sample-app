package com.example.bookmanager.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AuthorTest {

    @Test
    fun `名前と生年月日が同じなら同一人物とみなす`() {
        val sameName = AuthorName.of("Same")
        val sameBirthDate = BirthDate.of(LocalDate.parse("1990-01-01"))
        val differentName = AuthorName.of("Different")

        val a = Author.create(sameName, sameBirthDate)
        val b = Author.create(sameName, sameBirthDate)
        val c = Author.create(differentName, sameBirthDate)

        assertTrue(a.isSamePerson(b))
        assertFalse(a.isSamePerson(c))
    }
}
