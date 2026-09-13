package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.port.inbound.TaskType
import com.example.bookmanager.application.port.outbound.IdempotencyRepository
import com.example.bookmanager.application.port.outbound.MessagePoller
import com.example.bookmanager.application.port.outbound.TaskNotified
import com.example.bookmanager.application.port.outbound.TaskNotifier
import org.springframework.stereotype.Service

/**
 * メッセージ駆動バッチの受信、冪等性管理、個別処理、タスク通知を実行する。
 */
@Service
class MessageDrivenBatchJobExecutor(
    private val messagePoller: MessagePoller,
    private val idempotencyRepository: IdempotencyRepository,
    private val taskNotifier: TaskNotifier,
) {
    fun <T : Any> execute(
        taskType: TaskType<T>,
        failureMessage: String,
        process: (T) -> Unit,
    ) {
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
                            exception.message ?: failureMessage,
                        ),
                    )
                }
                throw exception
            }
        }
    }
}
