package com.example.bookmanager.domain

import kotlin.jvm.JvmInline

/**
 * 書籍タイトルの値オブジェクト。
 */
@JvmInline
value class Title private constructor(val value: String) {
    companion object {
        private const val MAX_LENGTH = 255

        fun of(raw: String): Title {
            val normalized = raw.trim()
            if (normalized.isEmpty()) {
                throw DomainException(DomainErrorCode.INVALID_TITLE, "Title must not be blank")
            }
            if (normalized.length > MAX_LENGTH) {
                throw DomainException(DomainErrorCode.TITLE_TOO_LONG, "Title must be $MAX_LENGTH characters or less")
            }
            return Title(normalized)
        }
    }
}
