package com.example.bookmanager.application

import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.shared.Id
import com.example.bookmanager.support.book.seedDefaultBooks
import com.example.bookmanager.support.db.IntegrationTestSupport
import com.example.bookmanager.jooq.tables.BookAuthors.BOOK_AUTHORS
import com.example.bookmanager.jooq.tables.Books.BOOKS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class UpdateBookUseCaseIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var updateBookUseCase: UpdateBookUseCase

    @Test
    fun `書籍を上書き更新できる`() {
        val ids = seedDefaultBooks(dsl)

        val parameter = UpdateBookUseCase.Parameter.from(
            bookId = ids.bookId2,
            title = "羅生門 改訂版",
            price = BigDecimal("1500.00"),
            publishStatus = PublishStatus.PUBLISHED,
            authorIds = listOf(ids.authorId1.toAuthorId(), ids.authorId2.toAuthorId()),
        )

        val output = updateBookUseCase.exec(parameter)

        assertThat(output.id).isEqualTo(ids.bookId2)
        assertThat(output.title).isEqualTo(parameter.title)
        assertThat(output.publishStatus).isEqualTo(parameter.publishStatus.name)
        assertThat(output.authorIds).containsExactlyInAnyOrder(ids.authorId1, ids.authorId2)

        val saved = dsl.selectFrom(BOOKS).where(BOOKS.ID.eq(ids.bookId2)).fetchOne()
        assertThat(saved!!.title).isEqualTo(parameter.title)
        assertThat(saved.price).isEqualByComparingTo(parameter.price)
        assertThat(saved.publishStatus).isEqualTo(parameter.publishStatus.name)

        val relations = dsl.selectFrom(BOOK_AUTHORS).where(BOOK_AUTHORS.BOOK_ID.eq(ids.bookId2)).fetch()
        assertThat(relations).hasSize(2)
        assertThat(relations.map { it.authorId }).containsExactlyInAnyOrder(ids.authorId1, ids.authorId2)
    }

    private fun UUID.toAuthorId(): AuthorId = Id.generate { this }
}
