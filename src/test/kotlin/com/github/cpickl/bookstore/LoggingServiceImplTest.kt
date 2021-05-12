package com.github.cpickl.bookstore

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import com.github.cpickl.bookstore.common.toEnumeration
import mu.KLogger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoggingServiceImplTest {

    private lateinit var klog: KLogger
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var logging: LoggingServiceImpl
    private val body = "some test body"
    private val nonJsonType = MediaType.IMAGE_PNG_VALUE

    @BeforeEach
    fun `init mocks`() {
        klog = mock()
        request = mock()
        response = mock()
        logging = LoggingServiceImpl(klog)
    }

    @Nested
    inner class RequestTest {

        @Test
        fun `When request body with JSON content Then log body`() {
            wheneverRequest(request, headers = mapOf(CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE))

            logging.logRequest(request, body)

            verifySingleInfoLog(klog) { logMessage ->
                assertThat(logMessage).contains(body)
            }
        }

        @Test
        fun `When request body with non-JSON content Then don't log body`() {
            wheneverRequest(request, headers = mapOf(CONTENT_TYPE to nonJsonType))

            logging.logRequest(request, body)

            verifySingleInfoLog(klog) { logMessage ->
                assertThat(logMessage).doesNotContain(body)
            }
        }
    }

    @Nested
    inner class ResponseTest {

        @Test
        fun `When response body is JSON content Then log body`() {
            wheneverRequest(request)
            wheneverResponse(response, headers = mapOf(CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE))

            logging.logResponse(request, response, body)

            verifySingleInfoLog(klog) { logMessage ->
                assertThat(logMessage).contains(body)
            }
        }

        @Test
        fun `When response body is non-JSON content Then don't log body`() {
            wheneverRequest(request)
            wheneverResponse(response, headers = mapOf(CONTENT_TYPE to nonJsonType))

            logging.logResponse(request, response, body)

            verifySingleInfoLog(klog) { logMessage ->
                assertThat(logMessage).doesNotContain(body)
            }
        }
    }
}

private fun wheneverRequest(
    request: HttpServletRequest,
    headers: Map<String, String> = emptyMap(),
    method: String = "METHOD",
    requestUri: String = "requestUri",
) {
    whenever(request.headerNames).thenReturn(headers.keys.toList().toEnumeration())
    headers.forEach { (k, v) ->
        whenever(request.getHeaders(k)).thenReturn(listOf(v).toEnumeration())
    }
    whenever(request.method).thenReturn(method)
    whenever(request.requestURI).thenReturn(requestUri)
}

private fun wheneverResponse(
    response: HttpServletResponse,
    status: Int = 200,
    headers: Map<String, String> = emptyMap(),
) {
    whenever(response.status).thenReturn(status)
    whenever(response.headerNames).thenReturn(headers.keys)
    headers.forEach { (k, v) ->
        whenever(response.getHeaders(k)).thenReturn(listOf(v))
    }
}
