package com.github.cpickl.bookstore.domain

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
    val price: Amount,
) {
    companion object

    val authorName = author.authorPseudonym
}

data class Image(
    val id: Id,
    val data: ByteArray,
) {
    companion object {
        fun empty(id: Id = RandomIdGenerator.generate()) =
            Image(id, byteArrayOf(0, 0))
    }

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

    override fun toString() = "Image[id=$id]"
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
    fun toEuroCents(): Int {
        require(currency == Currency.Euro)
        return if (precision == 0) value else {
            value * (10.0.pow(precision)).toInt()
        }
    }

    companion object {
        fun euro(euro: Int) = euroCent(euro * 100)
        fun euroCent(cents: Int) = Amount(Currency.Euro, cents, 2)
    }

    val formatted by lazy {
        val numberPart = if (precision == 0) {
            value
        } else {
            val format = NumberFormat.getNumberInstance(Locale.ENGLISH).apply {
                minimumFractionDigits = precision
            }
            format.format(value.toDouble() / (10.0.pow(precision)))
        }
        "${currency.code} $numberPart"
    }
}
