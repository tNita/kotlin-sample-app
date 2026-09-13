package com.example.bookmanager.infrastructure.inbound.rest.integration.author

import com.example.bookmanager.infrastructure.inbound.rest.author.RegisterAuthorRequest
import com.example.bookmanager.support.db.IntegrationTestSupport
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
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthorRegisterApiIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `POST 著者を登録できる`() {
        val request =
            RegisterAuthorRequest(
                name = "正岡子規",
                birthDate = LocalDate.parse("1867-10-14"),
            )

        val mvcResult =
            mockMvc
                .perform(
                    post("/api/authors")
                        .contentType("application/json")
                        .content(
                            """
                            {
                              "name": "${request.name}",
                              "birthDate": "${request.birthDate}"
                            }
                            """.trimIndent(),
                        ),
                ).andExpect(status().isCreated)
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.name").value(request.name))
                .andExpect(jsonPath("$.affiliation").value(""))
                .andExpect(jsonPath("$.birthDate").value(request.birthDate.toString()))
                .andReturn()

        val createdId = mvcResult.response.contentAsString.let { parseId(it) }

        mockMvc
            .perform(get("/api/authors/search").param("id", createdId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(createdId.toString()))
            .andExpect(jsonPath("$[0].name").value(request.name))
            .andExpect(jsonPath("$[0].birthDate").value(request.birthDate.toString()))
    }

    @Test
    fun `255文字の所属を登録して検索できる`() {
        val affiliation = "あ".repeat(255)
        val response =
            mockMvc
                .perform(
                    post("/api/authors")
                        .contentType("application/json")
                        .content("""{"name":"所属の著者","birthDate":"1990-01-01","affiliation":"$affiliation"}"""),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.affiliation").value(affiliation))
                .andReturn()
        val id = parseId(response.response.contentAsString)
        mockMvc
            .perform(get("/api/authors/search").param("id", id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].affiliation").value(affiliation))
    }

    @Test
    fun `256文字の所属は登録できない`() {
        val affiliation = "あ".repeat(256)
        mockMvc
            .perform(
                post("/api/authors")
                    .contentType("application/json")
                    .content("""{"name":"所属の著者","birthDate":"1990-01-01","affiliation":"$affiliation"}"""),
            ).andExpect(status().isBadRequest)
        mockMvc
            .perform(get("/api/authors/search").param("name", "所属の著者"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    private fun parseId(json: String): UUID =
        UUID.fromString(
            Regex("\"id\"\\s*:\\s*\"([^\"]+)\"")
                .find(json)
                ?.groupValues
                ?.get(1)
                ?: error("id not found in json: $json"),
        )
}
