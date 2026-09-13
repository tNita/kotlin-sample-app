package com.example.bookmanager.application.port.inbound

import com.example.bookmanager.application.CommandBookOutput
import com.example.bookmanager.application.QueryBookOutput
import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.PublishStatus
import java.math.BigDecimal
import java.util.UUID

interface RegisterBookInputPort {
    fun execute(command: RegisterBookCommand): CommandBookOutput
}

data class RegisterBookCommand(
    val title: String,
    val price: BigDecimal,
    val publishStatus: PublishStatus = PublishStatus.UNPUBLISHED,
    val authorIds: List<AuthorId>,
)

interface UpdateBookInputPort {
    fun execute(command: UpdateBookCommand): CommandBookOutput
}

data class UpdateBookCommand(
    val bookId: UUID,
    val title: String,
    val price: BigDecimal,
    val publishStatus: PublishStatus,
    val authorIds: List<AuthorId>,
)

interface GetBookInputPort {
    fun execute(bookId: UUID): QueryBookOutput
}

interface SearchBookInputPort {
    fun execute(authorName: String): List<QueryBookOutput>
}
