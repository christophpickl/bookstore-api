package com.github.cpickl.bookstore.boundary

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.github.cpickl.bookstore.domain.BookUpdateRequest
import com.github.cpickl.bookstore.domain.Currency
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Money
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "BookList",
    description = "Simple wrapper around books.",
)
@JacksonXmlRootElement(localName = "books")
data class BooksDto(
    @Schema(
        description = "List of books.",
        required = true,
    )
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "book")
    val books: List<BookSimpleDto>
)

@Schema(
    name = "BookSimple",
    description = "Simplified representation of a book",
)
data class BookSimpleDto(
    @get:Schema(
        description = "Unique identifier of the book as a UUID.",
        example = "00000000-1111-2222-3333-444444444444",
        required = true,
    )
    val id: String,

    @get:Schema(
        description = "Simply the book title.",
        example = "Homo Sapiens - A brief history of humankind",
        required = true,
    )
    val title: String,
) {
    @Schema(
        description = "Path to get the full book details.",
        example = "/books/00000000-1111-2222-3333-444444444444",
        required = true,
    )
    @Suppress("unused")
    val detailLink: String = "/books/$id"
}

@Schema(
    name = "Book",
    description = "Full fledged representation of a book.",
)
@JacksonXmlRootElement(localName = "book")
data class BookDto(
    @get:Schema(
        description = "Unique identifier of the book as a UUID.",
        example = "00000000-1111-2222-3333-444444444444",
        required = true,
    )
    val id: String,

    @get:Schema(
        description = "Simply the book title.",
        example = "Homo Sapiens - A brief history of humankind",
        required = true,
    )
    @JacksonXmlCData
    val title: String,

    @get:Schema(
        description = "Longer descriptive text.",
        example = "A very good book indeed.",
        required = true,
    )
    @JacksonXmlCData
    val description: String,

    @get:Schema(
        description = "The actual selling price.",
        required = true,
    )
    val price: MoneyDto,

    @get:Schema(
        description = "Pseudonym of the published user.",
        example = "Uncle Bob",
        required = true,
    )
    val author: String
) {
    @Schema(
        description = "Path to get the book's cover image.",
        example = "/books/00000000-1111-2222-3333-444444444444",
        required = true,
    )
    @Suppress("unused")
    val coverLink: String = "/books/$id/cover"
}

@Schema(
    name = "BookCreateRequest",
)
data class BookCreateDto(

    @get:Schema(
        description = "Define the book title.",
        example = "Homo Sapiens - A brief history of humankind",
        required = true,
    )
    val title: String,

    @get:Schema(
        description = "Define the descriptive text.",
        example = "A very good book indeed.",
        required = true,
    )
    val description: String,

    @get:Schema(
        description = "Define the selling price.",
        required = true,
    )
    val price: MoneyRequestDto,
) {
    companion object
}

@Schema(
    name = "BookUpdateRequest",
)
data class BookUpdateDto(

    @get:Schema(
        description = "Define the book title.",
        example = "Homo Sapiens - A brief history of humankind",
        required = true,
    )
    val title: String,

    @get:Schema(
        description = "Define the descriptive text.",
        example = "A very good book indeed.",
        required = true,
    )
    val description: String,

    @get:Schema(
        description = "Define the selling price.",
        required = true,
    )
    val price: MoneyRequestDto,
) {
    companion object
}

@Schema(
    name = "Money",
    description = "Multi-currency amount object, avoiding floating point miscalculations (read).",
)
data class MoneyDto(

    @get:Schema(
        description = "ISO 4217 alpha code (uppercase, three letters).",
        example = "EUR",
        required = true,
    )
    val currencyCode: String,

    @get:Schema(
        description = "Total amount in cents.",
        example = "4299",
        required = true,
    )
    val value: Int,

    @get:Schema(
        description = "Decimal point shift for value.",
        example = "2",
        required = true,
    )
    val precision: Int,
)

@Schema(
    name = "MoneyRequest",
    description = "Same as money but doesn't support the precision attribute (write).",
)
data class MoneyRequestDto(
    @get:Schema(
        description = "ISO 4217 alpha code (uppercase, three letters)",
        example = "EUR",
        required = true,
    )
    val currencyCode: String,

    @get:Schema(
        description = "ISO 4217 alpha code (uppercase, three letters)",
        example = "3995",
        required = true,
    )
    val value: Int,
)
