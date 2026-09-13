package com.example.bookmanager.application.port.outbound

import com.example.bookmanager.application.port.inbound.TaskType

interface MessagePoller {
    fun <T : Any> poll(queueName: String, dataType: Class<T>, consumer: (PolledMessage<T>) -> Unit)
}

data class PolledMessage<T>(
    val id: String,
    val taskToken: String,
    val data: T,
)
