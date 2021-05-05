package com.github.cpickl.bookstore.domain

import org.springframework.stereotype.Service
import java.util.UUID

data class Id(
    val uuid: UUID
) {
    constructor(uuid: String) : this(UUID.fromString(uuid))

    companion object;

    override fun toString() = uuid.toString()

    operator fun unaryMinus() = uuid
    operator fun unaryPlus() = uuid.toString()
}

operator fun UUID.unaryPlus() = Id(this)

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

    class On(term: String) : Search() {
        init {
            require(term.trim().isNotEmpty())
        }

        val term = term.toLowerCase()

        override fun equals(other: Any?): Boolean {
            if (other !is On) return false
            return this.term == other.term
        }

        override fun hashCode() = term.hashCode()
    }
}
