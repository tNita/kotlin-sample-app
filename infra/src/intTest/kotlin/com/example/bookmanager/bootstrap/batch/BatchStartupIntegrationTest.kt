package com.example.bookmanager.bootstrap.batch

import com.example.bookmanager.BookManagerApplication
import com.example.bookmanager.application.port.inbound.RunAuthorUpdateBatchJobInputPort
import com.example.bookmanager.application.port.inbound.RunBookUpdateBatchJobInputPort
import com.example.bookmanager.application.port.outbound.BookRepository
import com.example.bookmanager.infrastructure.inbound.job.BatchJobRunner
import com.example.bookmanager.infrastructure.inbound.messaging.AuthorRegistrationSqsIntegrationConfig
import com.example.bookmanager.infrastructure.inbound.messaging.AuthorRegistrationSqsMessageReceiver
import com.example.bookmanager.infrastructure.inbound.rest.book.BookFindController
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.boot.SpringApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.context.WebApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BatchStartupIntegrationTest {
    @Test
    fun `専用起動では共通アダプタとRunnerを読み込みAPIとWebと常駐リスナーは起動しない`() {
        val application = batchApplication()
        application.addPrimarySources(listOf(JobConfiguration::class.java))

        application.run("--bookmanager.messaging.author-registration.enabled=true").use { context ->
            assertFalse(context is WebApplicationContext)
            assertTrue(context.getBeansOfType(BookManagerApplication::class.java).isEmpty())
            assertTrue(context.getBeansOfType(BookFindController::class.java).isEmpty())
            assertTrue(context.getBeansOfType(AuthorRegistrationSqsIntegrationConfig::class.java).isEmpty())
            assertTrue(context.getBeansOfType(AuthorRegistrationSqsMessageReceiver::class.java).isEmpty())
            assertEquals(1, context.getBeansOfType(BookRepository::class.java).size)
            assertEquals(1, context.getBeansOfType(BatchJobRunner::class.java).size)
            verify(context.getBean("testJob", RunBookUpdateBatchJobInputPort::class.java)).execute()
            verifyNoInteractions(context.getBean("testAuthorJob", RunAuthorUpdateBatchJobInputPort::class.java))
            assertEquals(0, SpringApplication.exit(context))
            assertFalse(context.isActive)
        }
    }

    @Test
    fun `author引数では著者バッチだけを実行する`() {
        val application = batchApplication()
        application.addPrimarySources(listOf(JobConfiguration::class.java))
        application.run("--bookmanager.batch.job=author").use { context ->
            assertFalse(context is WebApplicationContext)
            assertEquals(1, context.getBeansOfType(BatchJobRunner::class.java).size)
            verify(context.getBean("testAuthorJob", RunAuthorUpdateBatchJobInputPort::class.java)).execute()
            verifyNoInteractions(context.getBean("testJob", RunBookUpdateBatchJobInputPort::class.java))
            assertEquals(0, SpringApplication.exit(context))
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    class JobConfiguration {
        @Bean
        @Primary
        fun testAuthorJob(): RunAuthorUpdateBatchJobInputPort = mock(RunAuthorUpdateBatchJobInputPort::class.java)

        @Bean
        @Primary
        fun testJob(): RunBookUpdateBatchJobInputPort = mock(RunBookUpdateBatchJobInputPort::class.java)
    }
}
