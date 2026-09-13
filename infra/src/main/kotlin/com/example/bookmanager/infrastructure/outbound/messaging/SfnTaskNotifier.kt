package com.example.bookmanager.infrastructure.outbound.messaging

import com.example.bookmanager.application.port.outbound.TaskNotified
import com.example.bookmanager.application.port.outbound.TaskNotifier
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sfn.model.SendTaskFailureRequest
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest

@Component
class SfnTaskNotifier(
    private val sfnClient: SfnClient,
) : TaskNotifier {
    override fun execute(task: TaskNotified) {
        when (task) {
            is TaskNotified.Success -> sfnClient.sendTaskSuccess(
                SendTaskSuccessRequest.builder().taskToken(task.token).output("{}").build(),
            )
            is TaskNotified.Failure -> sfnClient.sendTaskFailure(
                SendTaskFailureRequest.builder().taskToken(task.token).error("TaskFailed").cause(task.cause).build(),
            )
        }
    }
}
