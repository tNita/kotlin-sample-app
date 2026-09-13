package com.example.bookmanager.infrastructure.inbound.messaging

import io.awspring.cloud.sqs.integration.SqsMessageDrivenChannelAdapter
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import kotlin.test.assertContentEquals
import kotlin.test.assertIs

class AuthorRegistrationSqsIntegrationConfigTest {

    @Test
    fun `SQS Integrationの受信アダプタを既存キュー向けに構成する`() {
        val config = AuthorRegistrationSqsIntegrationConfig("bookmanager-author-registration")

        val producer = config.authorRegistrationSqsMessageProducer(mock(SqsAsyncClient::class.java))

        val adapter = assertIs<SqsMessageDrivenChannelAdapter>(producer)
        assertContentEquals(arrayOf("bookmanager-author-registration"), adapter.queues)
    }
}
