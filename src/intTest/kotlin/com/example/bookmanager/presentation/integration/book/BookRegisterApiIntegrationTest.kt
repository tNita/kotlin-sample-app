package com.example.bookmanager.presentation.integration.book

import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.presentation.book.RegisterBookRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookRegisterApiIntegrationTest : IntegrationTestSupport() {
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
    fun `POST 書籍を登録できる`() {
        val request = RegisterBookRequest(
            title = "こころ",
            price = BigDecimal("1500.00"),
            publishStatus = PublishStatus.UNPUBLISHED,
            authorIds = listOf(ids.authorId1, ids.authorId2),
        )

        val mvcResult = mockMvc.perform(
            post("/api/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsBytes(request)),
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.title").value("こころ"))
            .andExpect(jsonPath("$.price").value(1500.0))
            .andExpect(jsonPath("$.publishStatus").value(PublishStatus.UNPUBLISHED.name))
            .andExpect(jsonPath("$.authorIds[0]").value(ids.authorId1.toString()))
            .andExpect(jsonPath("$.authorIds[1]").value(ids.authorId2.toString()))
            .andReturn()

        val createdId = mvcResult.response.contentAsString.let { parseId(it) }

        mockMvc.perform(get("/api/books/{id}", createdId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("こころ"))
            .andExpect(jsonPath("$.price").value(1500.0))
            .andExpect(jsonPath("$.publishStatus").value(PublishStatus.UNPUBLISHED.name))
            .andExpect(jsonPath("$.authors[0].id").value(ids.authorId1.toString()))
            .andExpect(jsonPath("$.authors[0].name").value("夏目漱石"))
            .andExpect(jsonPath("$.authors[0].birthDate").value("1867-02-09"))
            .andExpect(jsonPath("$.authors[1].id").value(ids.authorId2.toString()))
            .andExpect(jsonPath("$.authors[1].name").value("芥川龍之介"))
            .andExpect(jsonPath("$.authors[1].birthDate").value("1892-03-01"))
    }

    private fun parseId(json: String): UUID {
        val node = objectMapper.readTree(json)
        return UUID.fromString(node.get("id").asText())
    }
}
