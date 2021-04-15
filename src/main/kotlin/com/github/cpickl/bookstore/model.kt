package com.github.cpickl.bookstore

import java.text.NumberFormat
import java.util.*
import kotlin.math.pow

data class User(
    val authorPseudonym: String,
    val username: String,
    val passwordHash: String,
) {
    companion object
}

data class Book(
    val id: Int,
    val title: String,
    val description: String,
    val author: User,
    val coverImage: Image,
    val price: Amount,
) {
    companion object

    val authorName = author.authorPseudonym
}

data class Image(
    val id: String,
    val data: ByteArray,
) {
    companion object;

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

enum class Currency(
    val code: String,
) {
    Euro("EUR");

    companion object
}

data class Amount(
    val currency: Currency,
    val value: Int,
    val precision: Int,
) {
    companion object {
        fun euro(euro: Int) = Amount(Currency.Euro, euro * 100, 2)
        fun euroCent(cents: Int) = Amount(Currency.Euro, cents, 0)
    }

    val formatted by lazy {
        "${currency.code} ${if(precision == 0) value else {
            val format = NumberFormat.getNumberInstance(Locale.ENGLISH).apply { 
                minimumFractionDigits = precision
            }
            format.format(value.toDouble() / (10.0.pow(precision)))
        }}"
    }
}
