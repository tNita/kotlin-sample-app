package com.example.bookmanager.application.port.outbound

data class BookUpdateBatchLineResult(
    val lineNumber: Int,
    val bookId: String,
    val updated: Boolean,
)
