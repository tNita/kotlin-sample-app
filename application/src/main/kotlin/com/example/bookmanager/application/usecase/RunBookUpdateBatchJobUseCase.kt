package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.port.inbound.GetBookInputPort
import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort
import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort.UpdateTaskData
import com.example.bookmanager.application.port.inbound.UpdateBookCommand
import com.example.bookmanager.application.port.inbound.UpdateBookInputPort
import com.example.bookmanager.application.port.outbound.BookUpdateBatchLineResult
import com.example.bookmanager.application.port.outbound.RawBookUpdateBatchLine
import com.example.bookmanager.application.port.outbound.RemoteStorage
import com.example.bookmanager.domain.AuthorId
import com.example.bookmanager.domain.PublishStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class RunBookUpdateBatchJobUseCase(
    private val batchExecutor: MessageDrivenBatchJobExecutor,
    private val getBook: GetBookInputPort,
    private val updateBook: UpdateBookInputPort,
    private val storage: RemoteStorage,
) : RunBookUpdateBatchJobInputPort {
    override fun execute() {
        batchExecutor.execute(UpdateTaskData.TASK_TYPE, "書籍一括更新に失敗しました", ::process)
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
                book.authorIds.map { AuthorId.generate { it } },
            ),
        )
    }

    private fun outputFilePath(data: UpdateTaskData) = "${data.outputDirectoryPath.trimEnd('/')}/book-update-result-${data.id}.jsonl"
}
