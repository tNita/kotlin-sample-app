package com.example.bookmanager.infrastructure.inbound.rest.author

import com.example.bookmanager.application.AuthorResult
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.UUID

@Schema(description = "著者登録リクエスト")
data class RegisterAuthorRequest(
    @field:NotBlank
    @field:Size(max = 255)
    @Schema(description = "著者名", example = "夏目漱石")
    val name: String,
    @field:NotNull
    @field:Past
    @Schema(description = "生年月日", type = "string", format = "date", example = "1867-02-09")
    val birthDate: LocalDate,
)

@Schema(description = "著者レスポンス")
data class AuthorResponse(
    @Schema(description = "著者ID", example = "018d1a2e-3b34-780a-a516-8a3f8a4f9a11")
    val id: UUID,
    @Schema(description = "著者名", example = "夏目漱石")
    val name: String,
    @Schema(description = "生年月日", type = "string", format = "date", example = "1867-02-09")
    val birthDate: LocalDate,
)

fun AuthorResult.toResponse(): AuthorResponse = AuthorResponse(id = id, name = name, birthDate = birthDate)
