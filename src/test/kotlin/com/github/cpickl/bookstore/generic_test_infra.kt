package com.github.cpickl.bookstore

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.cpickl.bookstore.boundary.Jwt
import mu.KLogger
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.capture
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import java.net.URI

val jackson = jacksonObjectMapper()

fun Assert<ResponseEntity<*>>.isOk() {
    isStatus(HttpStatus.OK)
}

fun Assert<ResponseEntity<*>>.isNotFound() {
    isStatus(HttpStatus.NOT_FOUND)
}

fun Assert<ResponseEntity<*>>.isForbidden() {
    isStatus(HttpStatus.FORBIDDEN)
}

fun Assert<ResponseEntity<*>>.isBadRequest() {
    isStatus(HttpStatus.BAD_REQUEST)
}

fun Assert<ResponseEntity<*>>.isStatus(code: HttpStatus) {
    given {
        assertThat(it.statusCode).isEqualTo(code)
    }
}

fun TestRestTemplate.requestGet(
    path: String,
    headers: HttpHeaders = HttpHeaders.EMPTY,
): ResponseEntity<String> =
    requestAny(HttpMethod.GET, path, headers = headers)

fun TestRestTemplate.requestPost(
    path: String,
    body: Any? = null,
    headers: HttpHeaders = HttpHeaders.EMPTY,
): ResponseEntity<String> =
    requestAny(HttpMethod.POST, path, body, headers)

fun TestRestTemplate.requestPut(
    path: String,
    body: Any? = null,
    headers: HttpHeaders = HttpHeaders.EMPTY,
): ResponseEntity<String> =
    requestAny(HttpMethod.PUT, path, body, headers)

fun TestRestTemplate.requestDelete(
    path: String,
    body: Any? = null,
    headers: HttpHeaders = HttpHeaders.EMPTY,
): ResponseEntity<String> =
    requestAny(HttpMethod.DELETE, path, body, headers)

inline fun <reified BODY : Any> TestRestTemplate.requestAny(
    method: HttpMethod,
    path: String,
    body: Any? = null,
    headers: HttpHeaders = HttpHeaders.EMPTY,
): ResponseEntity<BODY> =
    exchange(RequestEntity<Any>(body, headers, method, URI(path)))

fun HttpHeaders.readAuthorization() =
    this[HttpHeaders.AUTHORIZATION]!!.first().replace("Bearer ", "")

inline fun <reified T> ResponseEntity<String>.read(status: HttpStatus? = HttpStatus.OK): T {
    if (status != null) {
        assertThat(this).isStatus(status)
    }
    assertThat(body).isNotNull()
    return jackson.readValue(body!!)
}

fun HttpHeaders.withJwt(jwt: Jwt) = apply {
    this[HttpHeaders.AUTHORIZATION] = "Bearer $jwt"
}

fun verifySingleInfoLog(klog: KLogger, function: (String) -> Unit) {
    val captor = createCaptor<() -> Any?>()
    verify(klog).info(capture(captor))
    val logMessageProvider: () -> Any? = captor.firstValue
    val logMessage = logMessageProvider()
    function(logMessage?.toString() ?: "null")
    verifyNoMoreInteractions(klog)
}

private inline fun <reified T : Any> createCaptor(): ArgumentCaptor<T> =
    ArgumentCaptor.forClass(T::class.java)
