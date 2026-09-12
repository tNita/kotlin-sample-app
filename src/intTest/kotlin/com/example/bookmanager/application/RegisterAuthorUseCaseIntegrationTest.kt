package com.example.bookmanager.application

import com.example.bookmanager.application.RegisterAuthorUseCase.Parameter
import com.example.bookmanager.support.db.IntegrationTestSupport
import com.example.bookmanager.jooq.tables.Authors.AUTHORS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class RegisterAuthorUseCaseIntegrationTest : IntegrationTestSupport() {
    @Autowired
    private lateinit var registerAuthorUseCase: RegisterAuthorUseCase

    @Test
    fun `著者を登録できる`() {
        val parameter = Parameter(
            name = "川端康成",
            birthDate = LocalDate.parse("1899-06-14")
        )

        val result = registerAuthorUseCase.exec(parameter)

        assertThat(result.name).isEqualTo(parameter.name)
        assertThat(result.birthDate).isEqualTo(parameter.birthDate)

        val saved = dsl.selectFrom(AUTHORS).where(AUTHORS.ID.eq(result.id)).fetchOne()
        assertThat(saved).isNotNull
        assertThat(saved!!.name).isEqualTo(parameter.name)
        assertThat(saved.birthDate).isEqualTo(parameter.birthDate)
    }

    @Test
    fun `重複登録はエラーになる`() {
        val parameter = Parameter(
            name = "太宰治",
            birthDate = LocalDate.parse("1909-06-19")
        )
        registerAuthorUseCase.exec(parameter)

        val ex = assertThrows<ApplicationException> {
            registerAuthorUseCase.exec(parameter)
        }
        assertThat(ex.code).isEqualTo(ApplicationErrorCode.INVALID_REQUEST)
    }
}
