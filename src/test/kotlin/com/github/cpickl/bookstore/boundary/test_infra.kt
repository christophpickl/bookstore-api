package com.github.cpickl.bookstore.boundary

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.cpickl.bookstore.jackson
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.*
import java.net.URI
import java.util.*

fun BookCreateRequestDto.Companion.any() = BookCreateRequestDto(
    title = "title",
    description = "description",
    euroCents = 12,
)

fun LoginDto.Companion.any() = LoginDto(
    username = "user",
    password = "pass",
)

fun Assert<ResponseEntity<*>>.isOk() {
    isStatus(HttpStatus.OK)
}

fun Assert<ResponseEntity<*>>.isForbidden() {
    isStatus(HttpStatus.FORBIDDEN)
}

fun Assert<ResponseEntity<*>>.isStatus(code: HttpStatus) {
    given {
        assertThat(it.statusCode).isEqualTo(code)
    }
}

fun TestRestTemplate.get(path: String, headers: HttpHeaders = HttpHeaders.EMPTY): ResponseEntity<String> =
    any(HttpMethod.GET, path, headers = headers)

fun TestRestTemplate.post(
    path: String,
    body: Any? = null,
    headers: HttpHeaders = HttpHeaders.EMPTY
): ResponseEntity<String> =
    any(HttpMethod.POST, path, body, headers)

fun TestRestTemplate.any(
    method: HttpMethod,
    path: String,
    body: Any? = null,
    headers: HttpHeaders = HttpHeaders.EMPTY
): ResponseEntity<String> =
    exchange(RequestEntity<Any>(body, headers, method, URI(path)))

fun HttpHeaders.readAuthorization() =
    this[HttpHeaders.AUTHORIZATION]!!.first().replace("Bearer ", "")

inline fun <reified T> ResponseEntity<String>.read(): T =
    jackson.readValue(body!!)

fun TestRestTemplate.login(dto: LoginDto): String {
    val response =
        post("/login", jackson.writeValueAsString(dto), HttpHeaders().apply {
            this[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
        })

    assertThat(response).isOk()
    return response.headers.readAuthorization()
}
