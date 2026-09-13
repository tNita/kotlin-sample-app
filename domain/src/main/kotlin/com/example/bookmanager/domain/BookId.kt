package com.example.bookmanager.domain

import com.example.bookmanager.shared.Id
import java.util.UUID

@JvmInline
value class BookId private constructor(val value: UUID) {
    companion object {
        fun of(value: UUID): BookId =
            BookId(Id.generate { value }.value)

        fun generate(): BookId = BookId(Id.generate().value)
    }
}
