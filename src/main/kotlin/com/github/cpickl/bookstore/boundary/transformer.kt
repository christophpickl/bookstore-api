package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookUpdateRequest
import com.github.cpickl.bookstore.domain.Currency
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Money

fun Book.toBookSimpleDto() = BookSimpleDto(
    id = id.toString(),
    title = title,
)

fun BookCreateDto.toBookCreateRequest(
    username: String
) = BookCreateRequest(
    username = username,
    title = title,
    description = description,
    price = price.toMoney(),
)

fun Book.toBookDto() = BookDto(
    id = id.toString(),
    title = title,
    description = description,
    price = price.toMoneyDto(),
    author = authorName,
)

fun Money.toMoneyDto() = MoneyDto(
    currencyCode = currency.code,
    value = value,
    precision = currency.precision,
)

fun MoneyRequestDto.toMoney() = Money(
    currency = Currency.of(currencyCode),
    value = value,
)

fun BookUpdateDto.toBookUpdateRequest(
    username: String,
    id: Id,
) = BookUpdateRequest(
    username = username,
    id = id,
    title = title,
    description = description,
    price = price.toMoney(),
)
