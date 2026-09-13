package com.example.bookmanager.application.port.outbound

data class RawBookUpdateBatchLine(
    val bookId: String,
    val publishStatus: String,
    val price: String,
)
