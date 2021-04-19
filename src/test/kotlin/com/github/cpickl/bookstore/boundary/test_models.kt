package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.Money

fun BookCreateDto.Companion.any() = BookCreateDto(
    title = "anyTitle",
    description = "anyDescription",
    price = Money.euroCent(490).toMoneyRequestDto(),
)

fun BookUpdateDto.Companion.any() = BookUpdateDto(
    title = "anyTitleUpdate",
    description = "anyDescriptionUpdate",
    price = Money.euroCent(990).toMoneyRequestDto(),
)

fun Money.toMoneyRequestDto() = MoneyRequestDto(
    currencyCode = currency.code,
    value = value,
)

fun LoginDto.Companion.any() = LoginDto(
    username = "anyUser",
    password = "anyPass",
)

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
