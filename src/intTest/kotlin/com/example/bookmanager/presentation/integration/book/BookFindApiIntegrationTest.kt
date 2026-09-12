package com.example.bookmanager.presentation.integration.book

import com.example.bookmanager.domain.PublishStatus
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
class BookFindApiIntegrationTest : IntegrationTestSupport() {
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
    fun `GET 単一書籍を取得できる`() {
        mockMvc.perform(get("/api/books/{id}", ids.bookId1))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.id").value(ids.bookId1.toString()))
            .andExpect(jsonPath("$.title").value("吾輩は猫である"))
            .andExpect(jsonPath("$.price").value(1200.0))
            .andExpect(jsonPath("$.publishStatus").value(PublishStatus.PUBLISHED.name))
            .andExpect(jsonPath("$.authors[0].id").value(ids.authorId1.toString()))
            .andExpect(jsonPath("$.authors[0].name").value("夏目漱石"))
            .andExpect(jsonPath("$.authors[0].birthDate").value("1867-02-09"))
    }
}
