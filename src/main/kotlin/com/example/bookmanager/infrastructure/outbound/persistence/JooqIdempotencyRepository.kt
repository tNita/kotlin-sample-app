package com.example.bookmanager.infrastructure.outbound.persistence

import com.example.bookmanager.application.port.outbound.IdempotencyRepository
import com.example.bookmanager.jooq.tables.IdempotencyEntries.IDEMPOTENCY_ENTRIES
import org.jooq.DSLContext
import org.jooq.impl.DSL.currentOffsetDateTime
import org.springframework.stereotype.Repository

@Repository
class JooqIdempotencyRepository(
    private val dsl: DSLContext,
) : IdempotencyRepository {
    // TODO: add support ttl
    override fun tryStart(key: String): Boolean =
        dsl.insertInto(IDEMPOTENCY_ENTRIES)
            .set(IDEMPOTENCY_ENTRIES.IDEMPOTENCY_KEY, key)
            .onConflictDoNothing()
            .execute() == 1

    override fun complete(key: String) {
        dsl.update(IDEMPOTENCY_ENTRIES)
            .set(IDEMPOTENCY_ENTRIES.COMPLETED_AT, currentOffsetDateTime())
            .where(IDEMPOTENCY_ENTRIES.IDEMPOTENCY_KEY.eq(key))
            .execute()
    }

    override fun release(key: String) {
        dsl.deleteFrom(IDEMPOTENCY_ENTRIES)
            .where(IDEMPOTENCY_ENTRIES.IDEMPOTENCY_KEY.eq(key))
            .and(IDEMPOTENCY_ENTRIES.COMPLETED_AT.isNull)
            .execute()
    }
}
