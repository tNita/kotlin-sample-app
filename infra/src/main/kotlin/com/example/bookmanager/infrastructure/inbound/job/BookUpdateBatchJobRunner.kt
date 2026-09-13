package com.example.bookmanager.infrastructure.inbound.job

import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
@ConditionalOnProperty("bookmanager.book-update-batch.enabled", havingValue = "true")
class BookUpdateBatchJobRunner(
    private val runBookUpdateBatchJob: RunBookUpdateBatchJobInputPort,
    private val applicationContext: ConfigurableApplicationContext,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        val exitCode =
            try {
                runBookUpdateBatchJob.execute()
                0
            } catch (exception: Exception) {
                1
            }
        SpringApplication.exit(applicationContext)
        exitProcess(exitCode)
    }
}
