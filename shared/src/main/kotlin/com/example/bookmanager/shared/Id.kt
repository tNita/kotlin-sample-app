package com.example.bookmanager.shared

import com.example.bookmanager.domain.DomainErrorCode
import com.example.bookmanager.domain.DomainException
import com.fasterxml.uuid.Generators
import java.util.UUID

/**
 * 汎用ID（UUID v7固定）。
 */
@JvmInline
value class Id private constructor(val value: UUID) {
    companion object {
        private val generator = Generators.timeBasedEpochGenerator()

        fun generate(uuidSupplier: () -> UUID = generator::generate): Id {
            val uuid = uuidSupplier()
            if (uuid.version() != 7) {
                throw DomainException(DomainErrorCode.INVALID_ID_VERSION, "ID must be a UUID version 7")
            }
            return Id(uuid)
        }
    }
}