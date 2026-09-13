package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.port.inbound.GetBookInputPort
import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort
import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort.UpdateTaskData
import com.example.bookmanager.application.port.outbound.BookUpdateBatchLineResult
import com.example.bookmanager.application.port.inbound.UpdateBookCommand
import com.example.bookmanager.application.port.inbound.UpdateBookInputPort
import com.example.bookmanager.application.port.outbound.RawBookUpdateBatchLine
import com.example.bookmanager.application.port.outbound.RemoteStorage
import com.example.bookmanager.application.port.outbound.IdempotencyRepository
import com.example.bookmanager.application.port.outbound.MessagePoller
import com.example.bookmanager.application.port.outbound.TaskNotified
import com.example.bookmanager.application.port.outbound.TaskNotifier
import com.example.bookmanager.domain.PublishStatus
import com.example.bookmanager.shared.Id
import java.math.BigDecimal
import java.util.UUID

class RunBookUpdateBatchJobUseCase(
    private val messagePoller: MessagePoller,
    private val idempotencyRepository: IdempotencyRepository,
    private val taskNotifier: TaskNotifier,
    private val getBook: GetBookInputPort,
    private val updateBook: UpdateBookInputPort,
    private val storage: RemoteStorage,
) : RunBookUpdateBatchJobInputPort {
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
                            exception.message ?: "書籍一括更新に失敗しました"
                        )
                    )
                }
                throw exception
            }
        }
    }


    private fun process(data: UpdateTaskData) {
        storage.openInput(data.inputFilePath).use { input ->
            storage.openOutput(outputFilePath(data)).use { output ->
                input.lines(RawBookUpdateBatchLine::class.java).forEachIndexed { index, rawLine ->
                    // TODO: support batch update if the file size becomes fat .
                    update(rawLine)
                    output.write(BookUpdateBatchLineResult(index + 1, rawLine.bookId, true))
                }
                output.complete()
            }
        }
    }

    private fun update(rawLine: RawBookUpdateBatchLine) {
        val book = getBook.execute(UUID.fromString(rawLine.bookId))
        updateBook.execute(
            UpdateBookCommand(
                book.id,
                book.title,
                BigDecimal(rawLine.price),
                PublishStatus.valueOf(rawLine.publishStatus),
                book.authorIds.map { Id.generate { it } },
            ),
        )
    }

    private fun outputFilePath(data: UpdateTaskData) =
        "${data.outputDirectoryPath.trimEnd('/')}/book-update-result-${data.id}.jsonl"
}
