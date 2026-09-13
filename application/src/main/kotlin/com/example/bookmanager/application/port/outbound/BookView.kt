package com.example.bookmanager.application.port.outbound

import java.math.BigDecimal
import java.util.UUID

/**
 * 参照系で返す読み取り専用の書籍モデル。
 * ドメインエンティティとは独立しており、投影やデノーマライズに使う。
 */
data class BookView(
    val id: UUID,
    val title: String,
    val price: BigDecimal,
    val publishStatus: String,
    val authors: List<AuthorView>,
)
