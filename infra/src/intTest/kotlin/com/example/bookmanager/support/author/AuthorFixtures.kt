package com.example.bookmanager.support.author

import com.example.bookmanager.jooq.tables.Authors.AUTHORS
import org.jooq.DSLContext
import java.time.LocalDate
import java.util.UUID

data class AuthorFixture(
    val id: UUID,
    val name: String,
    val birthDate: LocalDate,
) {
    companion object {
        fun natsume(): AuthorFixture = AuthorFixture(
            id = UUID.fromString("01890a3f-54c1-7f1e-8b10-cbc61ff35f4e"),
            name = "夏目漱石",
            birthDate = LocalDate.parse("1867-02-09"),
        )

        fun akutagawa(): AuthorFixture = AuthorFixture(
            id = UUID.fromString("01890a3f-54c1-7f1e-8b10-cbc61ff35f4f"),
            name = "芥川龍之介",
            birthDate = LocalDate.parse("1892-03-01"),
        )
    }
}

fun DSLContext.insert(fixture: AuthorFixture) {
    insertInto(AUTHORS)
        .set(AUTHORS.ID, fixture.id)
        .set(AUTHORS.NAME, fixture.name)
        .set(AUTHORS.BIRTH_DATE, fixture.birthDate)
        .execute()
}
