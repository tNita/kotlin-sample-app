package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.ApplicationErrorCode
import com.example.bookmanager.application.ApplicationException
import com.example.bookmanager.application.CommandBookOutput
import com.example.bookmanager.application.service.AuthorDomainService
import com.example.bookmanager.application.port.inbound.UpdateBookCommand
import com.example.bookmanager.application.port.inbound.UpdateBookInputPort
import com.example.bookmanager.application.port.outbound.BookRepository
import com.example.bookmanager.application.toCommandOutput
import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.Book
import com.example.bookmanager.domain.BookId
import com.example.bookmanager.domain.Price
import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.domain.Title
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

/**
 * 書籍の更新のユースケース。
 */
@Service
@Transactional
class UpdateBookUseCase(
    private val bookRepository: BookRepository,
    private val authorDomainService: AuthorDomainService,
) : UpdateBookInputPort {
    /** IDを元に書籍を全項目上書きする。 */
    override fun execute(command: UpdateBookCommand): CommandBookOutput =
        runUseCase {
            val bookId = BookId.of(command.bookId)
            val existing =
                bookRepository.findById(bookId)
                    ?: throw ApplicationException(
                        ApplicationErrorCode.BOOK_NOT_FOUND,
                        "Book not found: ${bookId.value}",
                    )

            val updated = applyUpdates(existing, command)
            val saved = bookRepository.save(updated)
            saved.toCommandOutput()
        }

    fun exec(parameter: Parameter): CommandBookOutput = execute(parameter.toCommand())

    private fun applyUpdates(
        book: Book,
        command: UpdateBookCommand,
    ): Book {
        val title = Title.of(command.title)
        val price = Price.of(command.price)
        val authorIds = command.authorIds

        authorDomainService.ensureAllExist(authorIds)

        return book
            .withTitle(title)
            .withPrice(price)
            .withPublishStatus(command.publishStatus)
            .withAuthors(authorIds)
    }

    data class Parameter(
        val bookId: UUID,
        val title: String,
        val price: BigDecimal,
        val publishStatus: PublishStatus,
        val authorIds: List<AuthorId>,
    ) {
        fun toCommand() = UpdateBookCommand(bookId, title, price, publishStatus, authorIds)

        companion object {
            fun from(
                bookId: UUID,
                title: String,
                price: BigDecimal,
                publishStatus: PublishStatus,
                authorIds: List<AuthorId>,
            ): Parameter =
                Parameter(
                    bookId = bookId,
                    title = title,
                    price = price,
                    publishStatus = publishStatus,
                    authorIds = authorIds,
                )
        }
    }
}
