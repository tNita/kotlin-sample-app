package com.example.bookmanager.support.book

import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.support.author.AuthorFixture
import com.example.bookmanager.support.author.insert
import org.jooq.DSLContext
import java.math.BigDecimal
import java.util.UUID

data class BookSeedIds(
    val authorId1: UUID,
    val authorId2: UUID,
    val bookId1: UUID,
    val bookId2: UUID,
)

/**
 * 代表的な著者2名＋書籍2冊を投入して、IDセットを返す。
 */
fun seedDefaultBooks(dsl: DSLContext): BookSeedIds {
    val author1 = AuthorFixture.natsume()
    val author2 = AuthorFixture.akutagawa()
    dsl.insert(author1)
    dsl.insert(author2)

    val bookId1 = UUID.fromString("018d1a2e-3b34-780a-a516-8a3f8a4f9a11")
    val bookId2 = UUID.fromString("018d1a2e-3b34-780a-a516-8a3f8a4f9a12")

    dsl.insert(
        BookFixture(
            id = bookId1,
            title = "吾輩は猫である",
            price = BigDecimal("1200.00"),
            publishStatus = PublishStatus.PUBLISHED,
        )
    )
    dsl.insert(
        BookFixture(
            id = bookId2,
            title = "羅生門",
            price = BigDecimal("900.00"),
            publishStatus = PublishStatus.UNPUBLISHED,
        )
    )
    dsl.insert(BookAuthorFixture(bookId = bookId1, authorId = author1.id))
    dsl.insert(BookAuthorFixture(bookId = bookId2, authorId = author2.id))

    return BookSeedIds(
        authorId1 = author1.id,
        authorId2 = author2.id,
        bookId1 = bookId1,
        bookId2 = bookId2,
    )
}
