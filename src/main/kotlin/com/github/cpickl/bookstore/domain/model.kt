package com.github.cpickl.bookstore.domain

import java.lang.IllegalArgumentException
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

data class User(
    val id: Id,
    val authorPseudonym: String,
    val username: String,
    val passwordHash: String,
) {
    companion object;

    override fun toString() = "User[username='$username',id=$id,authorPseudonym='$authorPseudonym']"
}

data class Book(
    val id: Id,
    val title: String,
    val description: String,
    val author: User,
    val price: Money,
    val state: BookState,
) {
    companion object

    val authorName = author.authorPseudonym
}

enum class BookState {
    Unpublished,
    Published;

    companion object
}

sealed class CoverImage(
    val bytes: ByteArray,
) {
    companion object

    object DefaultImage : CoverImage(
        CoverImage::class.java
            .getResourceAsStream("/bookstore/icon_default_book.png")!!
            .readAllBytes()!!
    )

    class CustomImage(
        bytes: ByteArray,
    ) : CoverImage(bytes)
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
