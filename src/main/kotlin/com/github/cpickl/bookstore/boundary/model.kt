package com.github.cpickl.bookstore.boundary

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "books")
data class BooksDto(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "book")
    val books: List<BookSimpleDto>
)

data class BookSimpleDto(
    /** Of type UUID. */
    val id: String,
    val title: String,
) {
    @Suppress("unused")
    val detailLink: String = "/books/$id"
}

@JacksonXmlRootElement(localName = "book")
data class BookDto(
    val id: String,
    @JacksonXmlCData
    val title: String,
    @JacksonXmlCData
    val description: String,
    val price: MoneyDto,
    /** Pseudonym of the published user. */
    val author: String
) {
    @Suppress("unused")
    val coverLink: String = "/books/$id/cover"
}

data class BookCreateDto(
    val title: String,
    val description: String,
    val price: MoneyDto,
) {
    companion object
}

data class BookUpdateDto(
    val title: String,
    val description: String,
    val price: MoneyDto,
) {
    companion object
}

data class MoneyDto(
    val currencyCode: String,
    val value: Int,
    val precision: Int,
)
