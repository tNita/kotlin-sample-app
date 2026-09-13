package com.example.bookmanager.infrastructure.inbound.rest.exception

import com.example.bookmanager.application.ApplicationException
import com.example.bookmanager.application.ApplicationErrorType
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.slf4j.LoggerFactory

@RestControllerAdvice
@Hidden
class RestExceptionHandler {
    private val logger = LoggerFactory.getLogger(RestExceptionHandler::class.java)

    @ExceptionHandler(ApplicationException::class)
    fun handleApplication(exception: ApplicationException): ResponseEntity<ErrorResponse> {
        val status = when (exception.code.type) {
            ApplicationErrorType.NOT_FOUND -> HttpStatus.NOT_FOUND
            ApplicationErrorType.CONFLICT -> HttpStatus.CONFLICT
            ApplicationErrorType.INVALID_REQUEST -> HttpStatus.BAD_REQUEST
        }
        val body = ErrorResponse(
            code = exception.code.name,
            message = exception.message ?: "リクエストを処理できませんでした",
        )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(exception: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = "INVALID_REQUEST",
            message = exception.message ?: "不正なリクエストです",
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(exception: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("予期しないエラーが発生しました", exception)
        val body = ErrorResponse(
            code = "INTERNAL_ERROR",
            message = "サーバでエラーが発生しました。時間をおいて再度お試しください。",
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}

@Schema(description = "エラーレスポンス")
data class ErrorResponse(
    @Schema(description = "エラーコード", example = "BOOK_NOT_FOUND")
    val code: String,
    @Schema(description = "エラーメッセージ", example = "出版済みの書籍を未出版に戻すことはできません")
    val message: String,
)
