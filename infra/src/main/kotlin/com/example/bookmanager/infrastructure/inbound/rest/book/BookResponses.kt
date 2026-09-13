package com.example.bookmanager.infrastructure.inbound.rest.book

import com.example.bookmanager.application.CommandBookOutput
import com.example.bookmanager.application.QueryBookOutput
import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.infrastructure.inbound.rest.author.AuthorResponse
import com.example.bookmanager.infrastructure.inbound.rest.author.toResponse
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "書籍詳細レスポンス（参照系）")
data class BookDetailResponse(
    @Schema(description = "書籍ID", example = "018d1a2e-3b34-780a-a516-8a3f8a4f9a11")
    val id: UUID,
    @Schema(description = "タイトル", example = "ノルウェイの森")
    val title: String,
    @Schema(description = "価格", example = "1980")
    val price: BigDecimal,
    @Schema(description = "出版状況", example = "PUBLISHED")
    val publishStatus: PublishStatus,
    @Schema(description = "著者情報の配列")
    val authors: List<AuthorResponse>,
)

@Schema(description = "書籍レスポンス（登録・更新）")
data class BookMutationResponse(
    @Schema(description = "書籍ID", example = "018d1a2e-3b34-780a-a516-8a3f8a4f9a11")
    val id: UUID,
    @Schema(description = "タイトル", example = "ノルウェイの森")
    val title: String,
    @Schema(description = "価格", example = "1980")
    val price: BigDecimal,
    @Schema(description = "出版状況", example = "PUBLISHED")
    val publishStatus: PublishStatus,
    @Schema(description = "著者IDの配列", example = "[\"018d1a2e-3b34-780a-a516-8a3f8a4f9a11\"]")
    val authorIds: List<UUID>,
)

fun QueryBookOutput.toDetailResponse(): BookDetailResponse =
    BookDetailResponse(
        id = id,
        title = title,
        price = price,
        publishStatus = PublishStatus.valueOf(publishStatus),
        authors = authors.map { it.toResponse() },
    )

fun CommandBookOutput.toMutationResponse(): BookMutationResponse =
    BookMutationResponse(
        id = id,
        title = title,
        price = price,
        publishStatus = PublishStatus.valueOf(publishStatus),
        authorIds = authorIds,
    )
