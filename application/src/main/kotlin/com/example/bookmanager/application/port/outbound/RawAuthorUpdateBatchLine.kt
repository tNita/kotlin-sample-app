package com.example.bookmanager.application.port.outbound

data class RawAuthorUpdateBatchLine(
    val authorId: String,
    val affiliation: String,
)
