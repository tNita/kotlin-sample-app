package com.example.bookmanager.application

import java.time.LocalDate
import java.util.UUID


// シンプルなのでReadとWriteで共通利用する
data class AuthorResult(
    val id: UUID,
    val name: String,
    val birthDate: LocalDate
)