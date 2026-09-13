package com.example.bookmanager.infrastructure.inbound.rest.author

import com.example.bookmanager.application.port.inbound.RegisterAuthorCommand
import com.example.bookmanager.application.port.inbound.RegisterAuthorInputPort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/authors")
@Validated
@Tag(name = "Authors", description = "著者管理API")
class AuthorRegisterController(
    private val registerAuthorUseCase: RegisterAuthorInputPort,
) {
    @PostMapping
    @Operation(
        summary = "著者登録",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "登録成功",
                content = [Content(schema = Schema(implementation = AuthorResponse::class))],
            ),
            ApiResponse(responseCode = "400", description = "リクエスト不正"),
            ApiResponse(responseCode = "409", description = "著者の重複"),
        ],
    )
    fun register(
        @Valid @RequestBody request: RegisterAuthorRequest,
    ): ResponseEntity<AuthorResponse> {
        val created =
            registerAuthorUseCase.execute(
                RegisterAuthorCommand(
                    name = request.name,
                    birthDate = request.birthDate,
                    affiliation = request.affiliation.orEmpty(),
                ),
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toResponse())
    }
}
