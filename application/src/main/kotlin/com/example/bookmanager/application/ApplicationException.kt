package com.example.bookmanager.application

/**
 * アプリケーション層のエラーコード。プレゼンテーション層でのレスポンス変換に利用する。
 */
enum class ApplicationErrorType {
    NOT_FOUND,
    CONFLICT,
    INVALID_REQUEST
}

enum class ApplicationErrorCode(val type: ApplicationErrorType) {
    AUTHOR_NOT_FOUND(ApplicationErrorType.NOT_FOUND),
    BOOK_NOT_FOUND(ApplicationErrorType.NOT_FOUND),
    INVALID_REQUEST(ApplicationErrorType.INVALID_REQUEST),
}

class ApplicationException(
    val code: ApplicationErrorCode,
    override val message: String
) : RuntimeException(message)
