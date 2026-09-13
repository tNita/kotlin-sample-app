package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.QueryBookOutput
import com.example.bookmanager.application.port.inbound.SearchBookInputPort
import com.example.bookmanager.application.port.outbound.BookQueryRepository
import com.example.bookmanager.application.toQueryOutput
import com.example.bookmanager.domain.AuthorName
import org.springframework.stereotype.Service

@Service
class SearchBookUseCase(
    private val bookQueryRepository: BookQueryRepository,
) : SearchBookInputPort {
    /**
     * 著者名から書籍を検索し、重複を排除して返す。
     */
    override fun execute(authorName: String): List<QueryBookOutput> =
        runUseCase {
            val name = AuthorName.of(authorName)
            bookQueryRepository
                .findByAuthorName(name)
                .map { book -> book.toQueryOutput() }
        }

    fun exec(authorName: String): List<QueryBookOutput> = execute(authorName)
}
