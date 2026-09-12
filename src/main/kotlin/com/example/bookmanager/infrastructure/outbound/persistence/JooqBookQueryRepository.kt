package com.example.bookmanager.infrastructure.outbound.persistence

import com.example.bookmanager.domain.AuthorName
import com.example.bookmanager.domain.BookId
import com.example.bookmanager.application.port.outbound.AuthorView
import com.example.bookmanager.application.port.outbound.BookQueryRepository
import com.example.bookmanager.application.port.outbound.BookView
import com.example.bookmanager.jooq.tables.Authors.AUTHORS
import com.example.bookmanager.jooq.tables.BookAuthors.BOOK_AUTHORS
import com.example.bookmanager.jooq.tables.Books.BOOKS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class JooqBookQueryRepository(
    private val dsl: DSLContext,
) : BookQueryRepository {
    override fun findById(id: BookId): BookView? {
        val records = dsl.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLISH_STATUS,
            AUTHORS.ID,
            AUTHORS.NAME,
            AUTHORS.BIRTH_DATE,
        )
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOK_AUTHORS.BOOK_ID.eq(BOOKS.ID))
            .join(AUTHORS).on(AUTHORS.ID.eq(BOOK_AUTHORS.AUTHOR_ID))
            .where(BOOKS.ID.eq(id.value))
            .orderBy(BOOKS.ID, AUTHORS.ID)
            .fetch()

        return BookView(
            id = records.first().get(BOOKS.ID)!!,
            title = records.first().get(BOOKS.TITLE)!!,
            price = records.first().get(BOOKS.PRICE)!!,
            publishStatus = records.first().get(BOOKS.PUBLISH_STATUS)!!,
            authors = records.mapNotNull { row ->
                AuthorView(
                    id = row.get(AUTHORS.ID)!!,
                    name = row.get(AUTHORS.NAME)!!,
                    birthDate = row.get(AUTHORS.BIRTH_DATE)!!,
                )
            }
        )

    }

    override fun findByAuthorName(authorName: AuthorName): List<BookView> {

        val records = dsl.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLISH_STATUS,
            AUTHORS.ID,
            AUTHORS.NAME,
            AUTHORS.BIRTH_DATE,
        )
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOK_AUTHORS.BOOK_ID.eq(BOOKS.ID))
            .join(AUTHORS).on(AUTHORS.ID.eq(BOOK_AUTHORS.AUTHOR_ID))
            .where(AUTHORS.NAME.eq(authorName.value))
            .fetch()

        return records.groupBy { it.get(BOOKS.ID)!! }.map { (_, rows) ->
            BookView(
                id = rows.first().get(BOOKS.ID)!!,
                title = rows.first().get(BOOKS.TITLE)!!,
                price = rows.first().get(BOOKS.PRICE)!!,
                publishStatus = rows.first().get(BOOKS.PUBLISH_STATUS)!!,
                authors = rows.mapNotNull { row ->
                    AuthorView(
                        id = row.get(AUTHORS.ID)!!,
                        name = row.get(AUTHORS.NAME)!!,
                        birthDate = row.get(AUTHORS.BIRTH_DATE)!!,
                    )})
        }
    }
}
