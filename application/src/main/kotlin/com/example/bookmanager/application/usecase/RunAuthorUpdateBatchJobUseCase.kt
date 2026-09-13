package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.port.inbound.RunAuthorUpdateBatchJobInputPort
import com.example.bookmanager.application.port.inbound.RunAuthorUpdateBatchJobInputPort.UpdateTaskData
import com.example.bookmanager.application.port.inbound.UpdateAuthorAffiliationCommand
import com.example.bookmanager.application.port.inbound.UpdateAuthorAffiliationInputPort
import com.example.bookmanager.application.port.outbound.AuthorUpdateBatchLineResult
import com.example.bookmanager.application.port.outbound.IdempotencyRepository
import com.example.bookmanager.application.port.outbound.MessagePoller
import com.example.bookmanager.application.port.outbound.RawAuthorUpdateBatchLine
import com.example.bookmanager.application.port.outbound.RemoteStorage
import com.example.bookmanager.application.port.outbound.TaskNotified
import com.example.bookmanager.application.port.outbound.TaskNotifier
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RunAuthorUpdateBatchJobUseCase(
    private val messagePoller: MessagePoller,
    private val idempotencyRepository: IdempotencyRepository,
    private val taskNotifier: TaskNotifier,
    private val updateAuthor: UpdateAuthorAffiliationInputPort,
    private val storage: RemoteStorage,
) : RunAuthorUpdateBatchJobInputPort {
    override fun execute() {
        val taskType = UpdateTaskData.TASK_TYPE
        messagePoller.poll(taskType.queueName, taskType.dataType) { message ->
            val idempotencyKey = "${taskType.queueName}:${message.id}"
            if (!idempotencyRepository.tryStart(idempotencyKey)) return@poll
            try {
                process(message.data)
                taskNotifier.execute(TaskNotified.Success(message.taskToken))
                idempotencyRepository.complete(idempotencyKey)
            } catch (exception: Exception) {
                idempotencyRepository.release(idempotencyKey)
                runCatching {
                    taskNotifier.execute(
                        TaskNotified.Failure(
                            message.taskToken,
                            exception.message ?: "著者所属一括更新に失敗しました",
                        ),
                    )
                }
                throw exception
            }
        }
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
