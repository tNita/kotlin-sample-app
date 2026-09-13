package com.example.bookmanager.infrastructure.inbound.job

import com.example.bookmanager.application.port.inbound.RunAuthorUpdateBatchJobInputPort
import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.stereotype.Component

@Component
class BatchJobRunner(
    private val runBookUpdateBatchJob: RunBookUpdateBatchJobInputPort,
    private val runAuthorUpdateBatchJob: RunAuthorUpdateBatchJobInputPort,
) : ApplicationRunner,
    ExitCodeGenerator {
    private var result = 0

    override fun run(args: ApplicationArguments) {
        result =
            try {
                val jobs = args.getOptionValues("bookmanager.batch.job")
                require(jobs == null || jobs.size == 1) { "bookmanager.batch.job は1つ指定してください" }
                val value = jobs?.single() ?: "book"
                val job =
                    BatchJob.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                        ?: throw IllegalArgumentException("未対応のバッチです: $value（BOOK または AUTHOR を指定してください）")
                when (job) {
                    BatchJob.BOOK -> runBookUpdateBatchJob.execute()
                    BatchJob.AUTHOR -> runAuthorUpdateBatchJob.execute()
                }
                0
            } catch (exception: Exception) {
                logger.error("一括更新バッチに失敗しました", exception)
                1
            }
    }

    override fun getExitCode(): Int = result

    companion object {
        private val logger = LoggerFactory.getLogger(BatchJobRunner::class.java)
    }
}

enum class BatchJob {
    BOOK,
    AUTHOR,
}
