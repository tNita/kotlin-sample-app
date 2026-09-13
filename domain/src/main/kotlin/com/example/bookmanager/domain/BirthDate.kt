package com.example.bookmanager.domain

import java.time.Clock
import java.time.LocalDate
import kotlin.jvm.JvmInline

/**
 * 生年月日の値オブジェクト。未来日は許可しない。
 */
@JvmInline
value class BirthDate private constructor(val value: LocalDate) {
    companion object {
        fun of(value: LocalDate, clock: Clock = Clock.systemDefaultZone()): BirthDate {
            val today = LocalDate.now(clock)
            if (value.isAfter(today)) {
                throw DomainException(DomainErrorCode.BIRTHDATE_IN_FUTURE, "Birth date must not be in the future")
            }
            return BirthDate(value)
        }
    }
}
