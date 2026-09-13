package com.example.bookmanager.application.port.outbound

data class AuthorUpdateBatchLineResult(
    val lineNumber: Int,
    val authorId: String,
    val updated: Boolean,
)
