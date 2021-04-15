package com.github.cpickl.bookstore

fun Currency.Companion.any() = Currency.Euro

fun Amount.Companion.any() = Amount.euro(12)

fun Image.Companion.any() = Image("imageId", byteArrayOf(0, 1))

fun User.Companion.any() = User(
    authorPseudonym = "any author",
    username = "anyUsername",
    passwordHash = "passwordHash"
)

fun Book.Companion.any() = Book(
    id = 42,
    title = "any title",
    description = "any description",
    author = User.any(),
    coverImage = Image.any(),
    price = Amount.any()
)
