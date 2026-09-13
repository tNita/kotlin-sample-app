package com.example.bookmanager.application.usecase

import com.example.bookmanager.application.port.inbound.TaskType
import com.example.bookmanager.application.port.outbound.IdempotencyRepository
import com.example.bookmanager.application.port.outbound.MessagePoller
import com.example.bookmanager.application.port.outbound.TaskNotified
import com.example.bookmanager.application.port.outbound.TaskNotifier

/** バッチの受信・冪等性管理・完了通知を共通の手順で実行する。 */
abstract class AbstractBatchJobUseCase<T : Any>(
    private val messagePoller: MessagePoller,
    private val idempotencyRepository: IdempotencyRepository,
    private val taskNotifier: TaskNotifier,
    private val taskType: TaskType<T>,
    private val failureMessage: String,
) {
    fun execute() {
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

    protected abstract fun process(data: T)
}
