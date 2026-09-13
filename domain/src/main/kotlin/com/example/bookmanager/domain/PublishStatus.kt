package com.example.bookmanager.domain

/**
 * 書籍の出版状況。
 */
enum class PublishStatus {
    UNPUBLISHED,
    PUBLISHED;

    fun canTransitionTo(target: PublishStatus): Boolean {
        return !(this == PUBLISHED && target == UNPUBLISHED)
    }
}
