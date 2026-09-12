package com.example.bookmanager.application

import com.example.bookmanager.support.book.seedDefaultBooks
import com.example.bookmanager.support.db.IntegrationTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SearchBookUseCaseIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var searchBookUseCase: SearchBookUseCase

    @BeforeEach
    fun setup() {
        seedDefaultBooks(dsl)
    }

    @Test
    fun `著者名で書籍を検索できる`() {
        val results = searchBookUseCase.exec("夏目漱石")

        assertThat(results).hasSize(1)
        val book = results.single()
        assertThat(book.title).isEqualTo("吾輩は猫である")
        assertThat(book.authors).hasSize(1)
        assertThat(book.authors.single().name).isEqualTo("夏目漱石")
    }
}
