package com.example.bookmanager.application.port.inbound

import java.util.UUID

interface RunBookUpdateBatchJobInputPort {
    fun execute()

    data class UpdateTaskData(
        val id: UUID,
        val inputFilePath: String,
        val outputDirectoryPath: String,
    ) {
        companion object {
            val TASK_TYPE = TaskType("bookmanager-book-update-batch", UpdateTaskData::class.java)
        }
    }
}

data class TaskType<T : Any>(
    val queueName: String,
    val dataType: Class<T>,
)
