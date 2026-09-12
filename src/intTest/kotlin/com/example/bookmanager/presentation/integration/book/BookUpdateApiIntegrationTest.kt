package com.example.bookmanager.presentation.integration.book

import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.presentation.book.UpdateBookRequest
import com.example.bookmanager.support.book.BookSeedIds
import com.example.bookmanager.support.book.seedDefaultBooks
import com.example.bookmanager.support.db.IntegrationTestSupport
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookUpdateApiIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    private lateinit var ids: BookSeedIds

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
        ids = seedDefaultBooks(dsl)
    }

    @Test
    fun `PUT 書籍を更新できる`() {
        val request = UpdateBookRequest(
            title = "改訂版 羅生門",
            price = BigDecimal("1800.00"),
            publishStatus = PublishStatus.PUBLISHED,
            authorIds = listOf(ids.authorId2),
        )

        mockMvc.perform(
            put("/api/books/{id}", ids.bookId2)
                .contentType("application/json")
                .content(objectMapper.writeValueAsBytes(request)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(ids.bookId2.toString()))
            .andExpect(jsonPath("$.title").value("改訂版 羅生門"))
            .andExpect(jsonPath("$.price").value(1800.0))
            .andExpect(jsonPath("$.publishStatus").value(PublishStatus.PUBLISHED.name))
            .andExpect(jsonPath("$.authorIds[0]").value(ids.authorId2.toString()))
    }
}
