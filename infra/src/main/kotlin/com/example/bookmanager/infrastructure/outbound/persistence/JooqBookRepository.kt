package com.example.bookmanager.infrastructure.outbound.persistence

import com.example.bookmanager.application.port.outbound.BookRepository
import com.example.bookmanager.domain.Book
import com.example.bookmanager.domain.BookId
import com.example.bookmanager.domain.Price
import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.domain.Title
import com.example.bookmanager.jooq.tables.BookAuthors.Companion.BOOK_AUTHORS
import com.example.bookmanager.jooq.tables.Books.Companion.BOOKS
import com.example.bookmanager.jooq.tables.records.BooksRecord
import com.example.bookmanager.shared.Id
import org.jooq.DSLContext
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
@Primary
class JooqBookRepository(
    private val dsl: DSLContext,
) : BookRepository {
    override fun save(book: Book): Book {
        dsl
            .insertInto(BOOKS)
            .set(BOOKS.ID, book.id.value)
            .set(BOOKS.TITLE, book.title.value)
            .set(BOOKS.PRICE, book.price.amount)
            .set(BOOKS.PUBLISH_STATUS, book.publishStatus.name)
            .onConflict(BOOKS.ID)
            .doUpdate()
            .set(BOOKS.TITLE, book.title.value)
            .set(BOOKS.PRICE, book.price.amount)
            .set(BOOKS.PUBLISH_STATUS, book.publishStatus.name)
            .execute()
        replaceBookAuthors(dsl, book.id, book.authorIds)
        return book.copy(id = book.id)
    }

    override fun findById(id: BookId): Book? {
        val records =
            dsl
                .select(
                    BOOKS.ID,
                    BOOKS.TITLE,
                    BOOKS.PRICE,
                    BOOKS.PUBLISH_STATUS,
                    BOOK_AUTHORS.AUTHOR_ID,
                ).from(BOOKS)
                .leftJoin(BOOK_AUTHORS)
                .on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
                .where(BOOKS.ID.eq(id.value))
                .fetch()
        if (records.isEmpty()) return null

        val bookRecord = records[0].into(BOOKS)
        val authorIds =
            records
                .mapNotNull { it.get(BOOK_AUTHORS.AUTHOR_ID) }
                .map { uuid -> Id.generate { uuid } }
                .toSet()

        return bookRecord.toDomain(authorIds)
    }

    private fun replaceBookAuthors(
        tx: DSLContext,
        bookId: BookId,
        authorIds: Set<Id>,
    ) {
        tx
            .deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId.value))
            .execute()

        authorIds.forEach { authorId ->
            tx
                .insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookId.value)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId.value)
                .execute()
        }
    }

    private fun BooksRecord.toDomain(authorIds: Set<Id>): Book =
        Book.ofExisting(
            id = BookId.of(this.id!!),
            title = Title.of(this.title!!),
            price = Price.of(this.price ?: BigDecimal.ZERO),
            publishStatus = PublishStatus.valueOf(this.publishStatus!!),
            authorIds = authorIds,
        )
}
