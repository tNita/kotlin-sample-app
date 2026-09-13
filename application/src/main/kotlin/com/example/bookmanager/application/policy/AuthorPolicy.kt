package com.example.bookmanager.application.policy

import com.example.bookmanager.application.port.outbound.AuthorRepository
import com.example.bookmanager.domain.Author
import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.DomainErrorCode
import com.example.bookmanager.domain.DomainException
import org.springframework.stereotype.Service

/**
 * リポジトリを利用して著者に関する整合性を判定するドメインサービス。
 */
@Service
class AuthorPolicy(
    private val authorRepository: AuthorRepository,
) {
    fun ensureNotDuplicated(author: Author) {
        val alreadyExists = authorRepository.findByName(author.name).any { it.isSamePerson(author) }
        if (alreadyExists) {
            throw DomainException(DomainErrorCode.AUTHOR_DUPLICATE, "Author already registered")
        }
    }

    fun ensureAllExist(authorIds: Collection<AuthorId>) {
        val authors = authorRepository.findByIds(authorIds)
        return authorIds.forEach { authorId ->
            if (authors.none { it.id == authorId }) {
                throw DomainException(DomainErrorCode.AUTHOR_NOT_FOUND, "Author not found: $authorId")
            }
        }
    }
}
