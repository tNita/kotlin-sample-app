package com.example.bookmanager.domain

import com.example.bookmanager.shared.Id

/**
 * 著者エンティティ。
 */
data class Author(
    val id: Id,
    val name: AuthorName,
    val birthDate: BirthDate
) {
    companion object {
        fun create(name: AuthorName, birthDate: BirthDate): Author =
            Author(
                id = Id.generate(),
                name = name,
                birthDate = birthDate
            )

        fun ofExisting(id: Id, name: AuthorName, birthDate: BirthDate): Author =
            Author(id = id, name = name, birthDate = birthDate)
    }

    fun isSamePerson(other: Author): Boolean =
        name == other.name && birthDate == other.birthDate
}
