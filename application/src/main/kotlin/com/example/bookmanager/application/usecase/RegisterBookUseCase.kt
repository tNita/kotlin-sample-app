package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.CommandBookOutput
import com.example.bookmanager.application.service.AuthorDomainService
import com.example.bookmanager.application.port.inbound.RegisterBookCommand
import com.example.bookmanager.application.port.inbound.RegisterBookInputPort
import com.example.bookmanager.application.port.outbound.BookRepository
import com.example.bookmanager.application.toCommandOutput
import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.Book
import com.example.bookmanager.domain.Price
import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.domain.Title
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class RegisterBookUseCase(
    private val bookRepository: BookRepository,
    private val authorDomainService: AuthorDomainService,
) : RegisterBookInputPort {
    /**
     * 著者の存在を検証しつつ書籍を登録する。
     */
    override fun execute(command: RegisterBookCommand): CommandBookOutput =
        runUseCase {
            val title = Title.of(command.title)
            val price = Price.of(command.price)
            authorDomainService.ensureAllExist(command.authorIds)

            val book =
                Book.create(
                    title = title,
                    price = price,
                    publishStatus = command.publishStatus,
                    authorIds = command.authorIds,
                )

            val saved = bookRepository.save(book)
            saved.toCommandOutput()
        }

    fun exec(parameter: Parameter): CommandBookOutput = execute(parameter.toCommand())

    data class Parameter(
        val title: String,
        val price: BigDecimal,
        val publishStatus: PublishStatus = PublishStatus.UNPUBLISHED,
        val authorIds: List<AuthorId>,
    ) {
        fun toCommand() = RegisterBookCommand(title, price, publishStatus, authorIds)
    }
}
