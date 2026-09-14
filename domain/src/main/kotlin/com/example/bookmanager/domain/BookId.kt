package com.example.bookmanager.domain

import java.util.UUID

@JvmInline
value class BookId private constructor(
    val value: UUID,
) {
    companion object {
        fun of(value: UUID): BookId = BookId(requireIdVersion(value))

        fun generate(): BookId = BookId(Id.generate().value)
    }
}
