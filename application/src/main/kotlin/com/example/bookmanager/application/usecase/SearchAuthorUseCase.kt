package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.ApplicationErrorCode
import com.example.bookmanager.application.ApplicationException
import com.example.bookmanager.application.AuthorResult
import com.example.bookmanager.application.port.inbound.SearchAuthorInputPort
import com.example.bookmanager.application.port.outbound.AuthorQueryRepository
import com.example.bookmanager.application.toResult
import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.AuthorName
import com.example.bookmanager.shared.Id
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SearchAuthorUseCase(
    private val authorQueryRepository: AuthorQueryRepository,
) : SearchAuthorInputPort {
    /**
     * 著者IDまたは著者名(曖昧)で検索する。
     */
    override fun execute(
        id: UUID?,
        name: String?,
    ): List<AuthorResult> =
        runUseCase {
            val authorId: AuthorId? = id?.let { uuid -> Id.generate { uuid } }
            val authorName = name?.takeIf { it.isNotBlank() }?.let { AuthorName.of(it) }
            if (authorId == null && authorName == null) {
                throw ApplicationException(ApplicationErrorCode.INVALID_REQUEST, "Either id or name must be provided")
            }
            authorQueryRepository
                .search(authorId, authorName)
                .map { it.toResult() }
        }

    fun exec(
        id: UUID?,
        name: String?,
    ): List<AuthorResult> = execute(id, name)
}
