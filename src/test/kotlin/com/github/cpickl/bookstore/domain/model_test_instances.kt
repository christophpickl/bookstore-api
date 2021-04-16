package com.github.cpickl.bookstore.domain

import java.util.UUID

fun Id.Companion.any() = Id(uuid = UUID.randomUUID())

fun Currency.Companion.any() = Currency.Euro

fun Amount.Companion.any() = euro(12)

fun Image.Companion.any() = Image(RandomIdGenerator.generate(), byteArrayOf(0, 1))

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
    cover = Image.any(),
    price = Amount.any()
)

fun BookCreateRequest.Companion.any() = BookCreateRequest(
    username = "user",
    title = "title",
    description = "description",
    euroCent = 142,
)