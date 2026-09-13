package com.example.bookmanager.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PublishStatusTest {

    @Test
    fun `未出版から出版済みへ遷移できる`() {
        assertTrue(PublishStatus.UNPUBLISHED.canTransitionTo(PublishStatus.PUBLISHED))
    }

    @Test
    fun `出版済みから未出版へ遷移できない`() {
        assertFalse(PublishStatus.PUBLISHED.canTransitionTo(PublishStatus.UNPUBLISHED))
    }
}
