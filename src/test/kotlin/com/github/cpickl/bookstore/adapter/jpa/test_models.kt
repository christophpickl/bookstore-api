package com.github.cpickl.bookstore.adapter.jpa

import java.util.UUID

fun UserJpa.Companion.any() = UserJpa(
    id = UUID.randomUUID().toString(),
    authorPseudonym = "authorPseudonym",
    username = "username",
    passwordHash = "passwordHash",
)

fun BookJpa.Companion.any() = BookJpa(
    id = UUID.randomUUID().toString(),
    title = "title",
    description = "description",
    author = UserJpa.any(),
    currencyCode = "EUR",
    price = 42,
    state = BookStateJpa.UNPUBLISHED,
)
