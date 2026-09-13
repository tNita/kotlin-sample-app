package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.ApplicationErrorCode
import com.example.bookmanager.application.ApplicationException
import com.example.bookmanager.application.QueryBookOutput
import com.example.bookmanager.application.port.inbound.GetBookInputPort
import com.example.bookmanager.application.port.outbound.BookQueryRepository
import com.example.bookmanager.application.toQueryOutput
import com.example.bookmanager.domain.BookId
import java.util.UUID

class GetBookUseCase(
    private val bookQueryRepository: BookQueryRepository,
) : GetBookInputPort {
    /**
     * 書籍IDをキーに取得する。存在しない場合は業務エラーとする。
     */
    override fun execute(bookId: UUID): QueryBookOutput =
        runUseCase {
            val id = BookId.of(bookId)
            val book =
                bookQueryRepository.findById(id)
                    ?: throw ApplicationException(
                        ApplicationErrorCode.BOOK_NOT_FOUND,
                        "指定された書籍が見つかりません (id=$bookId)",
                    )
            book.toQueryOutput()
        }

    fun exec(bookId: UUID): QueryBookOutput = execute(bookId)
}
