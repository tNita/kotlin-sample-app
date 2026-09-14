package com.example.bookmanager.domain

import com.example.bookmanager.domain.AuthorId

/**
 * 著者エンティティ。
 */
data class Author(
    val id: AuthorId,
    val name: AuthorName,
    val birthDate: BirthDate,
    val affiliation: AuthorAffiliation = AuthorAffiliation.of("").getOrThrow(),
) {
    companion object {
        fun create(
            name: AuthorName,
            birthDate: BirthDate,
            affiliation: AuthorAffiliation = AuthorAffiliation.of("").getOrThrow(),
        ): Author =
            Author(
                id = AuthorId.generate(),
                name = name,
                birthDate = birthDate,
                affiliation = affiliation,
            )

        fun ofExisting(
            id: AuthorId,
            name: AuthorName,
            birthDate: BirthDate,
            affiliation: AuthorAffiliation = AuthorAffiliation.of("").getOrThrow(),
        ): Author = Author(id = id, name = name, birthDate = birthDate, affiliation = affiliation)
    }

    fun isSamePerson(other: Author): Boolean = name == other.name && birthDate == other.birthDate
}
