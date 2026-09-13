package com.example.bookmanager.bootstrap.batch

import com.example.bookmanager.application.port.inbound.GetBookInputPort
import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort
import com.example.bookmanager.application.port.inbound.UpdateBookInputPort
import com.example.bookmanager.application.port.outbound.IdempotencyRepository
import com.example.bookmanager.application.port.outbound.MessagePoller
import com.example.bookmanager.application.port.outbound.RemoteStorage
import com.example.bookmanager.application.port.outbound.TaskNotifier
import com.example.bookmanager.application.usecase.RunBookUpdateBatchJobUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BatchConfiguration {
    @Bean fun runBookUpdateBatchJobInputPort(
        messagePoller: MessagePoller,
        idempotencyRepository: IdempotencyRepository,
        taskNotifier: TaskNotifier,
        getBook: GetBookInputPort,
        updateBook: UpdateBookInputPort,
        storage: RemoteStorage,
    ): RunBookUpdateBatchJobInputPort =
        RunBookUpdateBatchJobUseCase(messagePoller, idempotencyRepository, taskNotifier, getBook, updateBook, storage)
}
