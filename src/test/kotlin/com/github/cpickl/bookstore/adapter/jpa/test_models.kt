package com.github.cpickl.bookstore.adapter.jpa

import java.util.UUID

fun UserJpa.Companion.any() = UserJpa(
    id = UUID.randomUUID().toString(),
    authorPseudonym = "authorPseudonym",
    username = "username",
    passwordHash = "passwordHash",
)
