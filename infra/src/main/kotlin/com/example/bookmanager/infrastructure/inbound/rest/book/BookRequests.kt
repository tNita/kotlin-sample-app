package com.example.bookmanager.infrastructure.inbound.rest.book

import com.example.bookmanager.application.port.inbound.RegisterBookCommand
import com.example.bookmanager.application.port.inbound.UpdateBookCommand
import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.shared.Id
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

@Schema(description = "書籍登録リクエスト")
data class RegisterBookRequest(
    @field:NotBlank
    @field:Size(max = 255)
    @Schema(description = "タイトル", example = "ノルウェイの森")
    val title: String,
    @field:DecimalMin(value = "0.0", inclusive = true)
    @field:Digits(integer = 10, fraction = 2)
    @field:NotNull
    @Schema(description = "価格", example = "1980")
    val price: BigDecimal,
    @field:NotNull
    @Schema(description = "出版状況", example = "UNPUBLISHED", defaultValue = "UNPUBLISHED")
    val publishStatus: PublishStatus = PublishStatus.UNPUBLISHED,
    @field:NotEmpty
    @Schema(description = "著者IDの配列", example = "[\"018d1a2e-3b34-780a-a516-8a3f8a4f9a11\"]")
    val authorIds: List<UUID>,
) {
    fun toCommand(): RegisterBookCommand =
        RegisterBookCommand(
            title = title,
            price = price,
            publishStatus = publishStatus,
            authorIds = authorIds.toAuthorIds(),
        )
}

@Schema(description = "書籍更新リクエスト（全項目を上書き）")
data class UpdateBookRequest(
    @field:NotBlank
    @field:Size(max = 255)
    @Schema(description = "タイトル", example = "新版 ノルウェイの森")
    val title: String,
    @field:DecimalMin(value = "0.0", inclusive = true)
    @field:Digits(integer = 10, fraction = 2)
    @field:NotNull
    @Schema(description = "価格", example = "2200")
    val price: BigDecimal,
    @field:NotNull
    @Schema(description = "出版状況", example = "PUBLISHED")
    val publishStatus: PublishStatus,
    @field:NotEmpty
    @Schema(description = "著者IDの配列", example = "[\"018d1a2e-3b34-780a-a516-8a3f8a4f9a11\"]")
    val authorIds: List<UUID>,
) {
    fun toCommand(bookId: UUID): UpdateBookCommand =
        UpdateBookCommand(
            bookId = bookId,
            title = title,
            price = price,
            publishStatus = publishStatus,
            authorIds = authorIds.toAuthorIds(),
        )
}

private fun List<UUID>.toAuthorIds(): List<AuthorId> = map { uuid -> Id.generate { uuid } }
