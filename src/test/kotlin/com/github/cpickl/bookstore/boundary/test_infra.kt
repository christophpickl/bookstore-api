package com.github.cpickl.bookstore.boundary

import assertk.Assert
import assertk.assertions.isEqualTo
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.*
import java.net.URI

fun Assert<ResponseEntity<*>>.isStatusCodeOk() {
    given {
        assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
    }
}

fun TestRestTemplate.get(path: String, headers: HttpHeaders = HttpHeaders()): ResponseEntity<String> =
    exchange(RequestEntity<Any>(headers, HttpMethod.GET, URI(path)))
