package com.example.bookmanager.presentation.integration.author

import com.example.bookmanager.support.author.AuthorFixture
import com.example.bookmanager.support.author.insert
import com.example.bookmanager.support.db.IntegrationTestSupport
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthorSearchApiIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
        dsl.insert(AuthorFixture.Companion.natsume())
        dsl.insert(AuthorFixture.Companion.akutagawa())
    }

    @Test
    fun `GET 著者名部分一致で著者を検索できる`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/authors/search")
                .param("name", "漱石"),
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("夏目漱石"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].birthDate").value("1867-02-09"))
    }

    @Test
    fun `GET 著者IDで著者を検索できる`() {
        val author = AuthorFixture.Companion.akutagawa()
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/authors/search")
                .param("id", author.id.toString()),
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(author.id.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(author.name))
    }
}
