package com.example.bookmanager.infrastructure.inbound.job

import com.example.bookmanager.application.port.inbound.RunAuthorUpdateBatchJobInputPort
import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.boot.DefaultApplicationArguments
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BatchJobRunnerTest {
    private val book = mock(RunBookUpdateBatchJobInputPort::class.java)
    private val author = mock(RunAuthorUpdateBatchJobInputPort::class.java)

    @Test
    fun `引数省略時は書籍だけ実行する`() {
        verifyExitCode(0)
        verify(book).execute()
        verifyNoInteractions(author)
    }

    @Test
    fun `book指定時は書籍だけ実行する`() {
        verifyExitCode(0, "--bookmanager.batch.job=book")
        verify(book).execute()
        verifyNoInteractions(author)
    }

    @Test
    fun `author指定時は著者だけ実行する`() {
        verifyExitCode(0, "--bookmanager.batch.job=AUTHOR")
        verify(author).execute()
        verifyNoInteractions(book)
    }

    @Test
    fun `書籍の失敗時はコード1を返す`() {
        doThrow(IllegalStateException("更新失敗")).`when`(book).execute()
        verifyExitCode(1)
        verifyNoInteractions(author)
    }

    @Test
    fun `著者の失敗時はコード1を返す`() {
        doThrow(IllegalStateException("更新失敗")).`when`(author).execute()
        verifyExitCode(1, "--bookmanager.batch.job=author")
        verifyNoInteractions(book)
    }

    @Test
    fun `不正な引数では実行せずコード1を返す`() {
        verifyExitCode(1, "--bookmanager.batch.job=unknown")
        verifyExitCode(1, "--bookmanager.batch.job")
        verifyExitCode(1, "--bookmanager.batch.job=book", "--bookmanager.batch.job=author")
        verifyNoInteractions(book, author)
    }

    private fun verifyExitCode(
        expected: Int,
        vararg args: String,
    ) {
        AnnotationConfigApplicationContext().use { context ->
            val runner = BatchJobRunner(book, author)
            context.beanFactory.registerSingleton("batchJobRunner", runner)
            context.refresh()
            runner.run(DefaultApplicationArguments(*args))
            assertTrue(context.isActive)
            assertEquals(expected, SpringApplication.exit(context))
            assertFalse(context.isActive)
        }
    }
}
