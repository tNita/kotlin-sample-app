package com.example.bookmanager.application.port.outbound

import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.AuthorName

/**
 * 参照専用の著者リポジトリ。
 */
interface AuthorQueryRepository {
    fun search(
        id: AuthorId?,
        name: AuthorName?,
    ): List<AuthorView>
}
