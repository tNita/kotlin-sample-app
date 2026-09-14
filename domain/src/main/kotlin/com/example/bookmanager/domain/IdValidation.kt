package com.example.bookmanager.domain

import java.util.UUID

internal fun requireIdVersion(value: UUID): UUID {
    if (value.version() != 7) {
        throw DomainException(DomainErrorCode.INVALID_ID_VERSION, "ID must be a UUID version 7")
    }
    return value
}
