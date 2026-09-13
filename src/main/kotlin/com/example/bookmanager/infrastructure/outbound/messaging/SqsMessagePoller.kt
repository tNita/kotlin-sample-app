package com.example.bookmanager.infrastructure.outbound.messaging

import com.example.bookmanager.application.port.outbound.MessagePoller
import com.example.bookmanager.application.port.outbound.PolledMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest

@Component
class SqsMessagePoller(
    private val sqsClient: SqsClient,
    private val objectMapper: ObjectMapper,
) : MessagePoller {
    override fun <T : Any> poll(queueName: String, dataType: Class<T>, consumer: (PolledMessage<T>) -> Unit) {
        val queueUrl = sqsClient.getQueueUrl { it.queueName(queueName) }.queueUrl()
        val message = sqsClient.receiveMessage(
            ReceiveMessageRequest.builder().queueUrl(queueUrl).maxNumberOfMessages(1).waitTimeSeconds(20).build(),
        ).messages().firstOrNull() ?: return
        val payload = objectMapper.readValue<Map<String, Any>>(message.body())
        val taskToken = requireNotNull(payload["taskToken"] as? String) { "taskToken は必須です" }
        val data = objectMapper.convertValue(requireNotNull(payload["data"]) { "data は必須です" }, dataType)
        consumer(PolledMessage(message.messageId(), taskToken, data))
        sqsClient.deleteMessage(
            DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(message.receiptHandle()).build(),
        )
    }
}
