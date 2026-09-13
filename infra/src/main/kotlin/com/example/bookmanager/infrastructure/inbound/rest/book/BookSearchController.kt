package com.example.bookmanager.infrastructure.inbound.rest.book

import com.example.bookmanager.application.QueryBookOutput
import com.example.bookmanager.application.port.inbound.SearchBookInputPort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/books")
@Validated
@Tag(name = "Books", description = "書籍管理API")
class BookSearchController(
    private val searchBookUseCase: SearchBookInputPort,
) {
    @GetMapping("/search")
    @Operation(
        summary = "著者名で書籍検索",
        responses = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "取得成功",
                content = [io.swagger.v3.oas.annotations.media.Content(
                    array = ArraySchema(schema = Schema(implementation = BookDetailResponse::class))
                )],
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "リクエスト不正"),
        ],
    )
    fun search(
        @Parameter(description = "著者名（完全一致）", example = "夏目漱石")
        @RequestParam @NotBlank authorName: String,
    ): List<BookDetailResponse> = searchBookUseCase.execute(authorName).map(QueryBookOutput::toDetailResponse)
}
