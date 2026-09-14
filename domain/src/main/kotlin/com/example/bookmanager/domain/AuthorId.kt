package com.example.bookmanager.domain

import java.util.UUID

@JvmInline
value class AuthorId private constructor(
    val value: UUID,
) {
    companion object {
        fun of(value: UUID): AuthorId = AuthorId(requireIdVersion(value))

        fun generate(uuidSupplier: () -> UUID = { Id.generate().value }): AuthorId = of(uuidSupplier())
    }
}
