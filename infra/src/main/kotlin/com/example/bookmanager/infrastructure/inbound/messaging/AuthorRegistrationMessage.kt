package com.example.bookmanager.infrastructure.inbound.messaging

import java.time.LocalDate

data class AuthorRegistrationMessage(
    val name: String,
    val birthDate: LocalDate,
)
