package com.example.bookmanager.application.port.outbound

import com.example.bookmanager.domain.Author
import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.AuthorName

interface AuthorRepository {
    fun save(author: Author): Author

    fun findById(id: AuthorId): Author?

    fun findByName(name: AuthorName): List<Author>

    fun findByIds(authorIds: Collection<AuthorId>): List<Author>
}
