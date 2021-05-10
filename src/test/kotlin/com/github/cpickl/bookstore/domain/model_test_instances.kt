package com.github.cpickl.bookstore.domain

import com.github.cpickl.bookstore.common.enumSetOf
import java.util.UUID

const val UUID1 = "00000000-0000-0000-0000-000000000001"
const val UUID2 = "00000000-0000-0000-0000-000000000002"

fun Id.Companion.any() = Id(uuid = UUID.randomUUID())
val Id.Companion.some1 get() = Id(UUID1)
val Id.Companion.some2 get() = Id(UUID2)

fun Currency.Companion.any() = Currency.Euro

fun Money.Companion.any() = euro(12)

fun CoverImage.Companion.any() = CoverImage.DefaultImage

fun User.Companion.any() = User(
    id = RandomIdGenerator.generate(),
    authorPseudonym = "any author",
    username = "anyUsername",
    passwordHash = "passwordHash",
    roles = enumSetOf(Role.User),
)

fun Book.Companion.any() = Book(
    id = RandomIdGenerator.generate(),
    title = "any title",
    description = "any description",
    author = Author.any(),
    price = Money.any(),
    state = BookState.any(),
)

fun BookState.Companion.any() = BookState.Unpublished

fun BookCreateRequest.Companion.any() = BookCreateRequest(
    username = "user",
    title = "title",
    description = "description",
    price = Money.euroCent(500),
)

fun Author.Companion.any() = Author(
    userId = Id.any(),
    pseudonym = "pseudonym",
)
