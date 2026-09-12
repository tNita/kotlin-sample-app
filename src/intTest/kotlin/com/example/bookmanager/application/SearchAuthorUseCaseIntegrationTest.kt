package com.example.bookmanager.application

import com.example.bookmanager.support.author.AuthorFixture
import com.example.bookmanager.support.author.insert
import com.example.bookmanager.support.db.IntegrationTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SearchAuthorUseCaseIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var searchAuthorUseCase: SearchAuthorUseCase

    @Test
    fun `IDで著者を検索できる`() {
        val author = AuthorFixture.akutagawa()
        dsl.insert(author)

        val results = searchAuthorUseCase.exec(id = author.id, name = null)

        assertThat(results).hasSize(1)
        assertThat(results.single().name).isEqualTo(author.name)
    }

    @Test
    fun `名前で著者を検索できる`() {
        val author = AuthorFixture.akutagawa()
        dsl.insert(author)

        val results = searchAuthorUseCase.exec(id = null, name = "芥川")

        assertThat(results).hasSize(1)
        assertThat(results.single().id).isEqualTo(author.id)
    }

    @Test
    fun `検索条件がない場合はエラーになる`() {
        val ex = assertThrows<ApplicationException> {
            searchAuthorUseCase.exec(id = null, name = null)
        }
        assertThat(ex.code).isEqualTo(ApplicationErrorCode.INVALID_REQUEST)
    }
}
