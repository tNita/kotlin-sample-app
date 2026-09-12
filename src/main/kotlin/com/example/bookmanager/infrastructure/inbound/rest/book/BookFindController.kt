package com.example.bookmanager.infrastructure.inbound.rest.book

import com.example.bookmanager.application.port.inbound.GetBookInputPort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/books")
@Validated
@Tag(name = "Books", description = "書籍管理API")
class BookFindController(
    private val getBookUseCase: GetBookInputPort,
) {
    @GetMapping("/{id}")
    @Operation(
        summary = "書籍詳細取得",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "取得成功",
                content = [Content(schema = Schema(implementation = BookDetailResponse::class))],
            ),
            ApiResponse(responseCode = "404", description = "書籍が存在しない"),
        ],
    )
    fun find(
        @Parameter(description = "書籍ID", example = "018d1a2e-3b34-780a-a516-8a3f8a4f9a11")
        @PathVariable id: UUID,
    ): ResponseEntity<BookDetailResponse> {
        val result = getBookUseCase.execute(id)
        return ResponseEntity.ok(result.toDetailResponse())
    }
}
