package com.example.bookmanager.infrastructure.config

import com.example.bookmanager.application.*
import com.example.bookmanager.application.port.inbound.*
import com.example.bookmanager.application.service.AuthorDomainService
import com.example.bookmanager.application.port.outbound.AuthorRepository
import com.example.bookmanager.application.port.outbound.BookRepository
import com.example.bookmanager.application.port.outbound.AuthorQueryRepository
import com.example.bookmanager.application.port.outbound.BookQueryRepository
import com.example.bookmanager.application.usecase.GetBookUseCase
import com.example.bookmanager.application.usecase.RegisterAuthorUseCase
import com.example.bookmanager.application.usecase.RegisterBookUseCase
import com.example.bookmanager.application.usecase.SearchAuthorUseCase
import com.example.bookmanager.application.usecase.SearchBookUseCase
import com.example.bookmanager.application.usecase.UpdateBookUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.support.TransactionTemplate

/** 外側のフレームワーク設定から、内側のユースケースを組み立てる。 */
@Configuration
class ApplicationConfiguration {
    @Bean fun authorDomainService(authorRepository: AuthorRepository) = AuthorDomainService(authorRepository)

    @Bean fun registerAuthorUseCase(authorRepository: AuthorRepository, authorDomainService: AuthorDomainService) = RegisterAuthorUseCase(authorRepository, authorDomainService)
    @Bean fun registerAuthorInputPort(useCase: RegisterAuthorUseCase, transactionTemplate: TransactionTemplate): RegisterAuthorInputPort = TransactionalRegisterAuthorInputPort(useCase, transactionTemplate)

    @Bean fun registerBookUseCase(bookRepository: BookRepository, authorDomainService: AuthorDomainService) = RegisterBookUseCase(bookRepository, authorDomainService)
    @Bean fun registerBookInputPort(useCase: RegisterBookUseCase, transactionTemplate: TransactionTemplate): RegisterBookInputPort = TransactionalRegisterBookInputPort(useCase, transactionTemplate)

    @Bean fun updateBookUseCase(bookRepository: BookRepository, authorDomainService: AuthorDomainService) = UpdateBookUseCase(bookRepository, authorDomainService)
    @Bean fun updateBookInputPort(useCase: UpdateBookUseCase, transactionTemplate: TransactionTemplate): UpdateBookInputPort = TransactionalUpdateBookInputPort(useCase, transactionTemplate)

    @Bean fun getBookUseCase(bookQueryRepository: BookQueryRepository) = GetBookUseCase(bookQueryRepository)
    @Bean fun searchBookUseCase(bookQueryRepository: BookQueryRepository) = SearchBookUseCase(bookQueryRepository)
    @Bean fun searchAuthorUseCase(authorQueryRepository: AuthorQueryRepository) = SearchAuthorUseCase(authorQueryRepository)
}

private class TransactionalRegisterAuthorInputPort(private val useCase: RegisterAuthorUseCase, private val transactionTemplate: TransactionTemplate) : RegisterAuthorInputPort {
    override fun execute(command: RegisterAuthorCommand) = transactionTemplate.execute { useCase.execute(command) }!!
}

private class TransactionalRegisterBookInputPort(private val useCase: RegisterBookUseCase, private val transactionTemplate: TransactionTemplate) : RegisterBookInputPort {
    override fun execute(command: RegisterBookCommand) = transactionTemplate.execute { useCase.execute(command) }!!
}

private class TransactionalUpdateBookInputPort(private val useCase: UpdateBookUseCase, private val transactionTemplate: TransactionTemplate) : UpdateBookInputPort {
    override fun execute(command: UpdateBookCommand) = transactionTemplate.execute { useCase.execute(command) }!!
}
