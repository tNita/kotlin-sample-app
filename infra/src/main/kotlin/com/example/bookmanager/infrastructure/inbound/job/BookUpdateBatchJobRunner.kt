package com.example.bookmanager.infrastructure.inbound.job

import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.stereotype.Component

@Component
class BookUpdateBatchJobRunner(
    private val runBookUpdateBatchJob: RunBookUpdateBatchJobInputPort,
) : ApplicationRunner,
    ExitCodeGenerator {
    private var result = 0

    override fun run(args: ApplicationArguments) {
        result =
            try {
                runBookUpdateBatchJob.execute()
                0
            } catch (exception: Exception) {
                logger.error("書籍一括更新バッチに失敗しました", exception)
                1
            }
    }

    override fun getExitCode(): Int = result

    companion object {
        private val logger = LoggerFactory.getLogger(BookUpdateBatchJobRunner::class.java)
    }
}
