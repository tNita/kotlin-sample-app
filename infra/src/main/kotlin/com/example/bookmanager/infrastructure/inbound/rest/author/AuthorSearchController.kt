package com.example.bookmanager.infrastructure.inbound.rest.author

import com.example.bookmanager.application.port.inbound.SearchAuthorInputPort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/authors")
@Validated
@Tag(name = "Authors", description = "著者管理API")
class AuthorSearchController(
    private val searchAuthorUseCase: SearchAuthorInputPort,
) {

    @GetMapping("/search")
    @Operation(
        summary = "著者名で著者を検索",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "取得成功",
                content = [io.swagger.v3.oas.annotations.media.Content(
                    array = ArraySchema(schema = Schema(implementation = AuthorResponse::class))
                )],
            ),
            ApiResponse(responseCode = "400", description = "リクエスト不正"),
        ],
    )
    fun search(
        @Parameter(description = "著者ID", example = "018d1a2e-3b34-780a-a516-8a3f8a4f9a11")
        @RequestParam(required = false) id: UUID?,
        @Parameter(description = "著者名（部分一致検索）", example = "漱石")
        @RequestParam(required = false) name: String?,
    ): List<AuthorResponse> =
        searchAuthorUseCase.execute(id = id, name = name).map { it.toResponse() }
}
