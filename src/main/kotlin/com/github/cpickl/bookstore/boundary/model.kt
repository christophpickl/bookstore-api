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
    /** Pseudonym of the published user. */
    val author: String,
    /** Preformatted amount. */
    val price: String, // FUTURE could keep Amount type and add custom jackson serializer
)

data class BookDto(
    val id: String,
    val title: String,
    @JacksonXmlCData
    val description: String,
    val price: String,
    val author: String,
    val coverLink: String,
)

data class BookCreateDto(
    val title: String,
    val description: String,
    val euroCent: Int,
) {
    companion object
}

data class BookUpdateDto(
    val title: String,
    val description: String,
    val euroCent: Int,
) {
    companion object
}
