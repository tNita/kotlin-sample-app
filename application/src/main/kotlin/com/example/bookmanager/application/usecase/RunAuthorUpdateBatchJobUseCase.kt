package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.port.inbound.RunAuthorUpdateBatchJobInputPort
import com.example.bookmanager.application.port.inbound.RunAuthorUpdateBatchJobInputPort.UpdateTaskData
import com.example.bookmanager.application.port.inbound.UpdateAuthorAffiliationCommand
import com.example.bookmanager.application.port.inbound.UpdateAuthorAffiliationInputPort
import com.example.bookmanager.application.port.outbound.AuthorUpdateBatchLineResult
import com.example.bookmanager.application.port.outbound.RawAuthorUpdateBatchLine
import com.example.bookmanager.application.port.outbound.RemoteStorage
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RunAuthorUpdateBatchJobUseCase(
    private val batchTaskExecutor: BatchTaskExecutor,
    private val updateAuthor: UpdateAuthorAffiliationInputPort,
    private val storage: RemoteStorage,
) : RunAuthorUpdateBatchJobInputPort {
    override fun execute() {
        batchTaskExecutor.execute(UpdateTaskData.TASK_TYPE, "著者所属一括更新に失敗しました", ::process)
    }

    private fun process(data: UpdateTaskData) {
        storage.openInput(data.inputFilePath).use { input ->
            storage.openOutput(outputFilePath(data)).use { output ->
                input.lines(RawAuthorUpdateBatchLine::class.java).forEachIndexed { index, rawLine ->
                    update(rawLine)
                    output.write(AuthorUpdateBatchLineResult(index + 1, rawLine.authorId, true))
                }
                output.complete()
            }
        }
    }

    private fun update(rawLine: RawAuthorUpdateBatchLine) {
        updateAuthor.execute(
            UpdateAuthorAffiliationCommand(UUID.fromString(rawLine.authorId), rawLine.affiliation),
        )
    }

    private fun outputFilePath(data: UpdateTaskData) = "${data.outputDirectoryPath.trimEnd('/')}/author-update-result-${data.id}.jsonl"
}
