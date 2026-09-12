package com.example.bookmanager.application

import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.shared.Id
import com.example.bookmanager.support.author.AuthorFixture
import com.example.bookmanager.support.author.insert
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
class RegisterBookUseCaseIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var registerBookUseCase: RegisterBookUseCase

    @Test
    fun `書籍を登録できる`() {
        val author = AuthorFixture.natsume()
        dsl.insert(author)

        val parameter = RegisterBookUseCase.Parameter(
            title = "こころ",
            price = BigDecimal("1800.00"),
            publishStatus = PublishStatus.PUBLISHED,
            authorIds = listOf(author.id.toAuthorId())
        )

        val output = registerBookUseCase.exec(parameter)

        assertThat(output.title).isEqualTo(parameter.title)
        assertThat(output.publishStatus).isEqualTo(parameter.publishStatus.name)
        assertThat(output.authorIds).containsExactly(author.id)

        val saved = dsl.selectFrom(BOOKS).where(BOOKS.ID.eq(output.id)).fetchOne()
        assertThat(saved).isNotNull
        assertThat(saved!!.title).isEqualTo(parameter.title)
        assertThat(saved.price).isEqualByComparingTo(parameter.price)
        assertThat(saved.publishStatus).isEqualTo(parameter.publishStatus.name)

        val relations = dsl.selectFrom(BOOK_AUTHORS).where(BOOK_AUTHORS.BOOK_ID.eq(output.id)).fetch()
        assertThat(relations).hasSize(1)
        assertThat(relations.single().authorId).isEqualTo(author.id)
    }

    private fun UUID.toAuthorId(): AuthorId = Id.generate { this }
}
