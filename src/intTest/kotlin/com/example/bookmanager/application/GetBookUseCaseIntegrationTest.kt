package com.example.bookmanager.application

import com.example.bookmanager.support.book.BookSeedIds
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
class GetBookUseCaseIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var getBookUseCase: GetBookUseCase

    private lateinit var ids: BookSeedIds

    @BeforeEach
    fun setup() {
        ids = seedDefaultBooks(dsl)
    }

    @Test
    fun `書籍をIDで取得できる`() {
        val output = getBookUseCase.exec(ids.bookId1)

        assertThat(output.id).isEqualTo(ids.bookId1)
        assertThat(output.title).isEqualTo("吾輩は猫である")
        assertThat(output.authors).hasSize(1)
        assertThat(output.authors.first().id).isEqualTo(ids.authorId1)
        assertThat(output.authorIds).containsExactly(ids.authorId1)
    }
}
