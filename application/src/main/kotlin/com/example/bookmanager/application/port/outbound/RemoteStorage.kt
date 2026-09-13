package com.example.bookmanager.application.port.outbound

interface RemoteStorage {
    fun openInput(path: String): RemoteInput

    fun openOutput(path: String): RemoteOutput
}

interface RemoteInput : AutoCloseable {
    fun <T : Any> lines(dataType: Class<T>): Sequence<T>
}

interface RemoteOutput : AutoCloseable {
    fun write(value: Any)

    fun complete()
}
