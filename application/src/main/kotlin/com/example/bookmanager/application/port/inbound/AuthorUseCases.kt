package com.example.bookmanager.application.port.inbound

import com.example.bookmanager.application.AuthorResult
import java.time.LocalDate
import java.util.UUID

interface RegisterAuthorInputPort {
    fun execute(command: RegisterAuthorCommand): AuthorResult
}

data class RegisterAuthorCommand(
    val name: String,
    val birthDate: LocalDate,
    val affiliation: String = "",
)

interface SearchAuthorInputPort {
    fun execute(
        id: UUID?,
        name: String?,
    ): List<AuthorResult>
}
