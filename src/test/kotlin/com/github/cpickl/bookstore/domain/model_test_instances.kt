package com.github.cpickl.bookstore.domain

import java.util.UUID

fun Id.Companion.any() = Id(uuid = UUID.randomUUID())

fun Currency.Companion.any() = Currency.Euro

fun Money.Companion.any() = euro(12)

fun CoverImage.Companion.any() = CoverImage.DefaultImage

fun User.Companion.any() = User(
    id = RandomIdGenerator.generate(),
    authorPseudonym = "any author",
    username = "anyUsername",
    passwordHash = "passwordHash"
)

fun Book.Companion.any() = Book(
    id = RandomIdGenerator.generate(),
    title = "any title",
    description = "any description",
    author = User.any(),
    price = Money.any(),
    state = BookState.any()
)

fun BookState.Companion.any() = BookState.Unpublished

fun BookCreateRequest.Companion.any() = BookCreateRequest(
    username = "user",
    title = "title",
    description = "description",
    price = Money.euroCent(500),
)
