package com.example.bookmanager.application.port.inbound

import com.example.bookmanager.application.AuthorResult
import java.util.UUID

interface RunAuthorUpdateBatchJobInputPort {
    fun execute()

    data class UpdateTaskData(
        val id: UUID,
        val inputFilePath: String,
        val outputDirectoryPath: String,
    ) {
        companion object {
            val TASK_TYPE = TaskType("bookmanager-author-update-batch", UpdateTaskData::class.java)
        }
    }
}

interface UpdateAuthorAffiliationInputPort {
    fun execute(command: UpdateAuthorAffiliationCommand): AuthorResult
}

data class UpdateAuthorAffiliationCommand(
    val authorId: UUID,
    val affiliation: String,
)
