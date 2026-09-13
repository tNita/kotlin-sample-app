package com.example.bookmanager.domain

/**
 * ドメインのルール違反や検証失敗を表す。
 */
class DomainException(
    val code: DomainErrorCode,
    override val message: String
) : IllegalArgumentException(message)

/**
 * ドメイン層のエラーコード。プレゼンテーション層でのレスポンス変換に利用する。
 */
enum class DomainErrorCode {
    INVALID_ID_VERSION,
    INVALID_AUTHOR_NAME,
    AUTHOR_NAME_TOO_LONG,
    AUTHOR_DUPLICATE,
    AUTHOR_NOT_FOUND,
    INVALID_TITLE,
    TITLE_TOO_LONG,
    PRICE_NEGATIVE,
    BIRTHDATE_IN_FUTURE,
    NO_AUTHORS,
    INVALID_PUBLISH_STATUS_TRANSITION
}
