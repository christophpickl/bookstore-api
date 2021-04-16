package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.jackson
import com.github.cpickl.bookstore.requestPost
import com.github.cpickl.bookstore.readAuthorization
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

fun BookCreateDto.Companion.any() = BookCreateDto(
    title = "anyTitle",
    description = "anyDescription",
    euroCents = 12,
)

fun BookUpdateDto.Companion.any() = BookUpdateDto(
    title = "anyTitleUpdate",
)

fun LoginDto.Companion.any() = LoginDto(
    username = "anyUser",
    password = "anyPass",
)

fun TestRestTemplate.login(dto: LoginDto): Jwt {
    val response = requestPost("/login", jackson.writeValueAsString(dto), HttpHeaders().apply {
        this[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
    })

    assertThat(response).isOk()
    return Jwt(response.headers.readAuthorization())
}

inline class Jwt(private val value: String) {
    override fun toString() = value
}

fun BookDetailDto.toBookListDto() = BookListDto(
    id = id,
    title = title,
    author = author,
    price = price,
)

fun Book.toBookListDto() = BookListDto(
    id = id.toString(),
    title = title,
    author = authorName,
    price = price.formatted
)

fun Book.toBookDetailDto() = BookDetailDto(
    id = id.toString(),
    title = title,
    author = authorName,
    price = price.formatted,
    description = description,
)
