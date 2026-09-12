package com.example.bookmanager.infrastructure.outbound.persistence

import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.AuthorName
import com.example.bookmanager.application.port.outbound.AuthorQueryRepository
import com.example.bookmanager.application.port.outbound.AuthorView
import com.example.bookmanager.jooq.tables.Authors.AUTHORS
import com.example.bookmanager.jooq.tables.records.AuthorsRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class JooqAuthorQueryRepository(
    private val dsl: DSLContext,
) : AuthorQueryRepository {

    override fun search(id: AuthorId?, name: AuthorName?): List<AuthorView> {
        val conditions = mutableListOf<Condition>()
        id?.let { conditions.add(AUTHORS.ID.eq(it.value)) }
        name?.let { conditions.add(AUTHORS.NAME.containsIgnoreCase(it.value)) }

        return dsl.selectFrom(AUTHORS)
            .where(conditions)
            .fetch()
            .map { it.toView() }
    }

    private fun AuthorsRecord.toView(): AuthorView = AuthorView(
            id = this.id!!,
            name = this.name!!,
            birthDate = this.birthDate!!,
        )
}
