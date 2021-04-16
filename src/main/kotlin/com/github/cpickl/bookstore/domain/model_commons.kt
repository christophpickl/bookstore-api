package com.github.cpickl.bookstore.domain

import org.springframework.stereotype.Service
import java.util.UUID

inline class Id(
    val uuid: UUID
) {
    constructor(uuid: String) : this (UUID.fromString(uuid))

    companion object;

    override fun toString() = uuid.toString()
}

// FUTURE could inject other in tests
interface IdGenerator {
    fun generate(): Id
}

@Service
object RandomIdGenerator : IdGenerator {
    override fun generate() = Id(UUID.randomUUID())
}
