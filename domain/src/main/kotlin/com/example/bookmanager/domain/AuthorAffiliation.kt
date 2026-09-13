package com.example.bookmanager.domain

/** 所属。空文字は未設定を表す。 */
@JvmInline
value class AuthorAffiliation private constructor(
    val value: String,
) {
    companion object {
        fun of(value: String): Result<AuthorAffiliation> =
            if (value.length <= 255) {
                Result.success(AuthorAffiliation(value))
            } else {
                Result.failure(DomainException(DomainErrorCode.AUTHOR_AFFILIATION_TOO_LONG, "所属は255文字以内で指定してください"))
            }
    }
}
