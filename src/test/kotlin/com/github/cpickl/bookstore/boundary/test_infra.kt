package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.Currency
import com.github.cpickl.bookstore.domain.Money
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
    price = Money.euroCent(490).toMoneyDto(),
)

fun BookUpdateDto.Companion.any() = BookUpdateDto(
    title = "anyTitleUpdate",
    description = "anyDescriptionUpdate",
    price = Money.euroCent(990).toMoneyDto(),
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

fun BookDto.toBookSimpleDto() = BookSimpleDto(
    id = id,
    title = title,
)

fun Book.toBookSimpleDto() = BookSimpleDto(
    id = id.toString(),
    title = title,
)

fun Book.toBookDto() = BookDto(
    id = id.toString(),
    title = title,
    author = authorName,
    price = price.toMoneyDto(),
    description = description,
)

fun Money.toMoneyDto() = MoneyDto(
    currencyCode = currency.code,
    value = value,
    precision = currency.precision,
)

fun MoneyDto.toMoney() = Money(
    currency = Currency.of(currencyCode),
    value = value,
)
