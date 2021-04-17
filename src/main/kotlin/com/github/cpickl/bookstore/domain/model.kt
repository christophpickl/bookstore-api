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
    val cover: Image,
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

data class Image(
    val id: Id, // FUTURE make use of image ID
    val bytes: ByteArray,
) {
    companion object {
        val default: ByteArray = Image::class.java
            .getResourceAsStream("/bookstore/icon_default_book.png")!!
            .readAllBytes()!!

        fun empty(id: Id = RandomIdGenerator.generate()) = Image(
            id = id,
            bytes = default,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (id != other.id) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }

    override fun toString() = "Image[id=$id]"
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

        fun format(currencyCode:String, value: Int, precision: Int): String {
            val numberPart = if (precision == 0) {
                value
            } else {
                val format = NumberFormat.getNumberInstance(Locale.ENGLISH).apply {
                    minimumFractionDigits = precision
                }
                format.format(value.toDouble() / (10.0.pow(precision)))
            }
            return "$currencyCode $numberPart"
        }
    }

    val formatted by lazy {
        format(currencyCode = currency.code, value = value, precision = currency.precision)
    }
}
