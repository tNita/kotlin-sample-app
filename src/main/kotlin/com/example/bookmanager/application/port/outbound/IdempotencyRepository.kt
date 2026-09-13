package com.example.bookmanager.application.port.outbound

interface IdempotencyRepository {
    fun tryStart(key: String): Boolean
    fun complete(key: String)
    fun release(key: String)
}
