package com.example.bookmanager.application

import com.example.bookmanager.application.port.outbound.AuthorView
import com.example.bookmanager.application.port.outbound.BookView
import com.example.bookmanager.domain.Author
import com.example.bookmanager.domain.Book
import java.math.BigDecimal
import java.util.UUID

/**
 * アプリケーション層から inbound アダプタへ返却するシンプルなレスポンスモデル。
 * ドメインの値オブジェクト／エンティティはそのまま漏らさない。
 */

data class CommandBookOutput(
    val id: UUID,
    val title: String,
    val price: BigDecimal,
    val publishStatus: String,
    val authorIds: List<UUID>,
)

data class QueryBookOutput(
    val id: UUID,
    val title: String,
    val price: BigDecimal,
    val publishStatus: String,
    val authorIds: List<UUID>,
    val authors: List<AuthorResult>,
)

fun Author.toResult(): AuthorResult =
    AuthorResult(
        id = this.id.value,
        name = this.name.value,
        birthDate = this.birthDate.value,
    )

fun AuthorView.toResult(): AuthorResult =
    AuthorResult(
        id = this.id,
        name = this.name,
        birthDate = this.birthDate,
    )

fun Book.toCommandOutput(): CommandBookOutput =
    CommandBookOutput(
        id = this.id.value,
        title = this.title.value,
        price = this.price.amount,
        publishStatus = this.publishStatus.name,
        authorIds = this.authorIds.map { it.value },
    )

fun BookView.toQueryOutput(): QueryBookOutput =
    QueryBookOutput(
        id = this.id,
        title = this.title,
        price = this.price,
        publishStatus = this.publishStatus,
        authorIds = this.authors.map { it.id },
        authors = this.authors.map { it.toResult() },
    )
