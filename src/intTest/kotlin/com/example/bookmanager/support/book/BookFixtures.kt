package com.example.bookmanager.support.book

import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.jooq.tables.Books.BOOKS
import org.jooq.DSLContext
import java.math.BigDecimal
import java.util.UUID

data class BookFixture(
    val id: UUID,
    val title: String,
    val price: BigDecimal,
    val publishStatus: PublishStatus,
)

fun DSLContext.insert(fixture: BookFixture) {
    insertInto(BOOKS)
        .set(BOOKS.ID, fixture.id)
        .set(BOOKS.TITLE, fixture.title)
        .set(BOOKS.PRICE, fixture.price)
        .set(BOOKS.PUBLISH_STATUS, fixture.publishStatus.name)
        .execute()
}
