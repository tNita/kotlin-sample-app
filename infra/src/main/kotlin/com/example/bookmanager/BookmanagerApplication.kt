package com.example.bookmanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

// API は常駐するため、起動時にバッチが実行されないよう、バッチの起動設定と Runner をスキャン対象から外す。
@SpringBootApplication
@ComponentScan(
    basePackages = [
        "com.example.bookmanager.infrastructure.inbound.rest",
        "com.example.bookmanager.infrastructure.inbound.messaging",
        "com.example.bookmanager.application",
        "com.example.bookmanager.infrastructure.config",
        "com.example.bookmanager.infrastructure.outbound",
    ],
)
class BookManagerApplication

fun main(args: Array<String>) {
    runApplication<BookManagerApplication>(*args)
}
