package com.example.bookmanager.infrastructure.inbound.job

import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.boot.DefaultApplicationArguments
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookUpdateBatchJobRunnerTest {
    @Test
    fun `成功時はコンテキストを維持し終了時にコード0を返す`() {
        verifyExitCode(mock(RunBookUpdateBatchJobInputPort::class.java), 0)
    }

    @Test
    fun `失敗時はコンテキストを維持し終了時にコード1を返す`() {
        val job = mock(RunBookUpdateBatchJobInputPort::class.java)
        doThrow(IllegalStateException("更新失敗")).`when`(job).execute()

        verifyExitCode(job, 1)
    }

    private fun verifyExitCode(
        job: RunBookUpdateBatchJobInputPort,
        expected: Int,
    ) {
        AnnotationConfigApplicationContext().use { context ->
            val runner = BookUpdateBatchJobRunner(job)
            context.beanFactory.registerSingleton("bookUpdateBatchJobRunner", runner)
            context.refresh()

            runner.run(DefaultApplicationArguments())

            verify(job).execute()
            assertTrue(context.isActive)
            assertEquals(expected, SpringApplication.exit(context))
            assertFalse(context.isActive)
        }
    }
}
