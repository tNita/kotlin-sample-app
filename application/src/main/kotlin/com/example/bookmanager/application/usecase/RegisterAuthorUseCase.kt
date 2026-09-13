package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.AuthorResult
import com.example.bookmanager.application.port.inbound.RegisterAuthorCommand
import com.example.bookmanager.application.port.outbound.AuthorRepository
import com.example.bookmanager.application.service.AuthorDomainService
import com.example.bookmanager.application.toResult
import com.example.bookmanager.domain.Author
import com.example.bookmanager.domain.AuthorName
import com.example.bookmanager.domain.BirthDate
import java.time.LocalDate

class RegisterAuthorUseCase(
    private val authorRepository: AuthorRepository,
    private val authorDomainService: AuthorDomainService,
) {
    /**
     * 著者の重複を避けつつ登録する。
     */
    fun execute(command: RegisterAuthorCommand): AuthorResult =
        runUseCase {
            val name = AuthorName.of(command.name)
            val birthDate = BirthDate.of(command.birthDate)
            val author = Author.create(name, birthDate)
            authorDomainService.ensureNotDuplicated(author)

            val saved = authorRepository.save(author)
            saved.toResult()
        }

    fun exec(parameter: Parameter): AuthorResult = execute(parameter.toCommand())

    data class Parameter(
        val name: String,
        val birthDate: LocalDate,
    ) {
        fun toCommand() = RegisterAuthorCommand(name, birthDate)
    }
}
