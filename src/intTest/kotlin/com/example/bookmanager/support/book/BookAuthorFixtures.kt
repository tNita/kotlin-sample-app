package com.example.bookmanager.support.book

import com.example.bookmanager.jooq.tables.BookAuthors.BOOK_AUTHORS
import org.jooq.DSLContext
import java.util.UUID

data class BookAuthorFixture(
    val bookId: UUID,
    val authorId: UUID,
)

fun DSLContext.insert(fixture: BookAuthorFixture) {
    insertInto(BOOK_AUTHORS)
        .set(BOOK_AUTHORS.BOOK_ID, fixture.bookId)
        .set(BOOK_AUTHORS.AUTHOR_ID, fixture.authorId)
        .execute()
}
