package com.example.bookmanager.application.port.outbound

import com.example.bookmanager.domain.Book
import com.example.bookmanager.domain.BookId

interface BookRepository {
    fun save(book: Book): Book
    fun findById(id: BookId): Book?
}
