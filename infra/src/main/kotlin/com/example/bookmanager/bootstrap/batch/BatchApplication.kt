package com.example.bookmanager.bootstrap.batch

import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import kotlin.system.exitProcess

// バッチは処理後に終了する専用プロセスのため、API や常駐リスナーを読み込まず、共通設定とバッチ用の Bean だけを組み立てる。
@Configuration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "com.example.bookmanager.infrastructure.inbound.job",
        "com.example.bookmanager.application",
        "com.example.bookmanager.infrastructure.config",
        "com.example.bookmanager.infrastructure.outbound",
    ],
)
class BatchApplication

fun batchApplication(): SpringApplication =
    SpringApplication(BatchApplication::class.java).apply {
        // API と同じモジュールに Web の依存があるため、バッチでは Web サーバーを明示的に無効化する。
        setWebApplicationType(WebApplicationType.NONE)
    }

fun main(args: Array<String>) {
    val context = batchApplication().run(*args)
    // Runner を JVM 終了から切り離し、プロセスを起動した main でリソースの解放と終了コードの返却を行う。
    exitProcess(SpringApplication.exit(context))
}
