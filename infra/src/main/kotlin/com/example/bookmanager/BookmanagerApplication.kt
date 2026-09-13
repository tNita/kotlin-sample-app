package com.example.bookmanager

import com.example.bookmanager.application.usecase.RunBookUpdateBatchJobUseCase
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

// API は常駐するため、起動時にバッチが実行されないよう、バッチの起動設定と Runner をスキャン対象から外す。
@SpringBootApplication
@ComponentScan(
    basePackages = [
        "com.example.bookmanager.infrastructure.inbound.rest",
        "com.example.bookmanager.infrastructure.inbound.messaging",
        "com.example.bookmanager.application.usecase",
        "com.example.bookmanager.application.service",
        "com.example.bookmanager.infrastructure.config",
        "com.example.bookmanager.infrastructure.outbound",
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [RunBookUpdateBatchJobUseCase::class],
        ),
    ],
)
class BookManagerApplication

fun main(args: Array<String>) {
    runApplication<BookManagerApplication>(*args)
}
