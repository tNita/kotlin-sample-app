package com.example.bookmanager.presentation.integration.book

import com.example.bookmanager.support.book.BookSeedIds
import com.example.bookmanager.support.book.seedDefaultBooks
import com.example.bookmanager.support.db.IntegrationTestSupport
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookSearchApiIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc
    private lateinit var ids: BookSeedIds

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
        ids = seedDefaultBooks(dsl)
    }

    @Test
    fun `GET 著者名検索で結果が返る`() {
        mockMvc.perform(
            get("/api/books/search")
                .param("authorName", "夏目漱石"),
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$[0].id").value(ids.bookId1.toString()))
            .andExpect(jsonPath("$[0].title").value("吾輩は猫である"))
            .andExpect(jsonPath("$[0].authors[0].id").value(ids.authorId1.toString()))
            .andExpect(jsonPath("$[0].authors[0].name").value("夏目漱石"))
            .andExpect(jsonPath("$[0].authors[0].birthDate").value("1867-02-09"))
    }
}
