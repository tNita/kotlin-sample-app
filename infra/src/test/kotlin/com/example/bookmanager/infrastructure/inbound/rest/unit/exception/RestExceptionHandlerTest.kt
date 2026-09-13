package com.example.bookmanager.infrastructure.inbound.rest.unit.exception

import com.example.bookmanager.application.ApplicationErrorCode
import com.example.bookmanager.application.ApplicationException
import com.example.bookmanager.infrastructure.inbound.rest.exception.RestExceptionHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.WebRequest

class RestExceptionHandlerTest {

    private val handler = RestExceptionHandler()

    @Test
    fun `ApplicationException maps to status code`() {
        val ex = ApplicationException(ApplicationErrorCode.AUTHOR_NOT_FOUND, "not found")

        val response = handler.handleApplication(ex)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("AUTHOR_NOT_FOUND", response.body?.code)
        assertEquals("not found", response.body?.message)
    }

    @Test
    fun `IllegalArgumentException maps to 400`() {
        val ex = IllegalArgumentException("bad")

        val response = handler.handleIllegalArgument(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("INVALID_REQUEST", response.body?.code)
        assertEquals("bad", response.body?.message)
    }

    @Test
    fun `Unexpected exception maps to 500`() {
        val ex = RuntimeException("boom")
        val request = mock(WebRequest::class.java)

        val response = handler.handleUnexpected(ex, request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("INTERNAL_ERROR", response.body?.code)
    }
}
