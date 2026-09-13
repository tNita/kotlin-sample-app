package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.ApplicationErrorCode
import com.example.bookmanager.application.ApplicationException
import com.example.bookmanager.application.AuthorResult
import com.example.bookmanager.application.port.inbound.UpdateAuthorAffiliationCommand
import com.example.bookmanager.application.port.inbound.UpdateAuthorAffiliationInputPort
import com.example.bookmanager.application.port.outbound.AuthorRepository
import com.example.bookmanager.application.toResult
import com.example.bookmanager.domain.AuthorAffiliation
import com.example.bookmanager.domain.AuthorId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpdateAuthorAffiliationUseCase(
    private val authorRepository: AuthorRepository,
) : UpdateAuthorAffiliationInputPort {
    override fun execute(command: UpdateAuthorAffiliationCommand): AuthorResult =
        runUseCase {
            val affiliation = AuthorAffiliation.of(command.affiliation).getOrThrow()
            val author =
                authorRepository.findById(AuthorId.generate { command.authorId })
                    ?: throw ApplicationException(ApplicationErrorCode.AUTHOR_NOT_FOUND, "著者が見つかりません: ${command.authorId}")
            authorRepository.update(author.copy(affiliation = affiliation)).toResult()
        }
}
