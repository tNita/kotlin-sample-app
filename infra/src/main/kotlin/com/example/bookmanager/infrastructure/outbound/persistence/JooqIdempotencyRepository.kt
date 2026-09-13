package com.example.bookmanager.infrastructure.outbound.persistence

import com.example.bookmanager.application.port.outbound.IdempotencyRepository
import com.example.bookmanager.jooq.tables.IdempotencyEntries.IDEMPOTENCY_ENTRIES
import org.jooq.DSLContext
import org.jooq.impl.DSL.currentOffsetDateTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.OffsetDateTime

@Repository
class JooqIdempotencyRepository(
    private val dsl: DSLContext,
    @Value("\${bookmanager.idempotency.ttl:PT1H}") private val ttl: Duration,
) : IdempotencyRepository {
    override fun tryStart(key: String): Boolean {
        val expiresAt = OffsetDateTime.now().plus(ttl)
        return dsl
            .insertInto(IDEMPOTENCY_ENTRIES)
            .set(IDEMPOTENCY_ENTRIES.IDEMPOTENCY_KEY, key)
            .set(IDEMPOTENCY_ENTRIES.EXPIRES_AT, expiresAt)
            .onConflict(IDEMPOTENCY_ENTRIES.IDEMPOTENCY_KEY)
            .doUpdate()
            .set(IDEMPOTENCY_ENTRIES.EXPIRES_AT, expiresAt)
            .where(
                IDEMPOTENCY_ENTRIES.COMPLETED_AT.isNull
                    .and(IDEMPOTENCY_ENTRIES.EXPIRES_AT.le(currentOffsetDateTime())),
            ).execute() == 1
    }

    override fun complete(key: String) {
        dsl
            .update(IDEMPOTENCY_ENTRIES)
            .set(IDEMPOTENCY_ENTRIES.COMPLETED_AT, currentOffsetDateTime())
            .where(IDEMPOTENCY_ENTRIES.IDEMPOTENCY_KEY.eq(key))
            .execute()
    }

    override fun release(key: String) {
        dsl
            .deleteFrom(IDEMPOTENCY_ENTRIES)
            .where(IDEMPOTENCY_ENTRIES.IDEMPOTENCY_KEY.eq(key))
            .and(IDEMPOTENCY_ENTRIES.COMPLETED_AT.isNull)
            .execute()
    }
}
