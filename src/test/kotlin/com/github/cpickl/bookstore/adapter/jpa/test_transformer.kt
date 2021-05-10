package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.common.toEnumSet
import com.github.cpickl.bookstore.domain.Author
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookState
import com.github.cpickl.bookstore.domain.Currency
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.Role
import com.github.cpickl.bookstore.domain.User

fun UserJpa.toUser() = User(
    id = Id(id),
    authorPseudonym = authorPseudonym,
    username = username,
    passwordHash = passwordHash,
    roles = roles.map { Role.byName(it.roleName) }.toEnumSet()
)

fun BookJpa.toBook() = Book(
    id = Id(id),
    title = title,
    description = description,
    author = author.toAuthor(),
    price = Money(
        currency = Currency.of(currencyCode),
        value = price,
    ),
    state = state.toBookState(),
)

fun UserJpa.toAuthor() = Author(
    userId = Id(id),
    pseudonym = authorPseudonym,
)

fun BookStateJpa.toBookState() = when (this) {
    BookStateJpa.UNPUBLISHED -> BookState.Unpublished
    BookStateJpa.PUBLISHED -> BookState.Published
}
