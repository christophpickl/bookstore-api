package com.github.cpickl.bookstore.domain

import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
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

enum class Currency(
    val code: String,
    val precision: Int
) {
    Euro("EUR", 2);

    companion object {
        private val currenciesByCode by lazy {
            values().associateBy { it.code }
        }

        fun of(currencyCode: String) =
            currenciesByCode[currencyCode] ?: throw IllegalArgumentException("Invalid currency code: '$currencyCode'!")
    }
}

data class Money(
    val currency: Currency,
    val value: Int,
) {
    companion object {
        fun euro(euro: Int) = euroCent(euro * @Suppress("MagicNumber") 100)
        fun euroCent(cents: Int) = Money(Currency.Euro, cents)
    }
}

sealed class Search {
    object Off : Search()

    // FUTURE support multiple terms (and wildcards)
    class On(term: String) : Search() {
        init {
            require(term.trim().isNotEmpty())
        }

        val term = term.toLowerCase() // FUTURE with kotlin 1.5 use lowercase()

        override fun equals(other: Any?): Boolean {
            if (other !is On) return false
            return this.term == other.term
        }

        override fun hashCode() = term.hashCode()
    }
}
