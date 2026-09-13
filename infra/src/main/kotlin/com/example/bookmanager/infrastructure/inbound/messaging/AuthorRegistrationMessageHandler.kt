package com.example.bookmanager.infrastructure.inbound.messaging

import com.example.bookmanager.application.port.inbound.RegisterAuthorCommand
import com.example.bookmanager.application.port.inbound.RegisterAuthorInputPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

interface AuthorRegistrationMessageConsumer {
    fun handle(message: AuthorRegistrationMessage)
}

@Component
class AuthorRegistrationMessageHandler(
    private val registerAuthorUseCase: RegisterAuthorInputPort,
) : AuthorRegistrationMessageConsumer {
    private val logger = LoggerFactory.getLogger(AuthorRegistrationMessageHandler::class.java)

    override fun handle(message: AuthorRegistrationMessage) {
        val registered =
            registerAuthorUseCase.execute(
                RegisterAuthorCommand(
                    name = message.name,
                    birthDate = message.birthDate,
                ),
            )
        logger.info("著者登録メッセージを処理しました: authorId={}", registered.id)
    }
}
