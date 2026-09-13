package com.example.bookmanager.application.port.outbound

interface TaskNotifier {
    fun execute(task: TaskNotified)
}

sealed interface TaskNotified {
    val token: String

    data class Success(
        override val token: String,
    ) : TaskNotified

    data class Failure(
        override val token: String,
        val cause: String,
    ) : TaskNotified
}
