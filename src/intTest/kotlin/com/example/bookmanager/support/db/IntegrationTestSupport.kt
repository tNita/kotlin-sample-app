package com.example.bookmanager.support.db

import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

/**
 * 統合テストの共通ベース。
 * 各テスト前にDBをクリーンアップし、継承先で必ず空の状態から開始させる。
 */
abstract class IntegrationTestSupport {

    @Autowired
    protected lateinit var dsl: DSLContext

    @BeforeEach
    fun cleanUp() {
        dsl.deleteAllTables()
    }
}
