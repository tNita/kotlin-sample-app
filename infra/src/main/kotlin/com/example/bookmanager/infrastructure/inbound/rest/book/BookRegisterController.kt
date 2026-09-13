package com.example.bookmanager.infrastructure.inbound.rest.book

import com.example.bookmanager.application.port.inbound.RegisterBookInputPort
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
@RequestMapping("/api/books")
@Validated
@Tag(name = "Books", description = "書籍管理API")
class BookRegisterController(
    private val registerBookUseCase: RegisterBookInputPort,
) {
    @PostMapping
    @Operation(
        summary = "書籍登録",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "登録成功",
                content = [Content(schema = Schema(implementation = BookMutationResponse::class))],
            ),
            ApiResponse(responseCode = "400", description = "リクエスト不正"),
            ApiResponse(responseCode = "404", description = "著者が存在しない"),
        ],
    )
    fun register(
        @Valid @RequestBody request: RegisterBookRequest,
    ): ResponseEntity<BookMutationResponse> {
        val created = registerBookUseCase.execute(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toMutationResponse())
    }
}
