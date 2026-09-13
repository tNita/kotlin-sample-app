package com.example.bookmanager.application.port.outbound

import com.example.bookmanager.domain.AuthorName
import com.example.bookmanager.domain.BookId

/**
 * 参照専用の書籍リポジトリ。
 * 読み取りに最適化したクエリモデルを返す。
 */
interface BookQueryRepository {
    fun findById(id: BookId): BookView?

    fun findByAuthorName(authorName: AuthorName): List<BookView>
}
