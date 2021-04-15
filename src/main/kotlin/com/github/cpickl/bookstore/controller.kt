package com.github.cpickl.bookstore

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/books", produces = [ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE])
class BooksController(
    private val service: BooksService
) {
    @GetMapping("")
    fun listAllBooks(): List<BookListDto> =
        service.getBooks().map { it.toBookListDto() }

}

fun Book.toBookListDto() = BookListDto(
    id = id,
    title = title,
    author = authorName,
    price = price.formatted // FUTURE render complex Amount object instead
)

data class BookListDto(
    val id: Int,
    val title: String,
    val author: String,
    val price: String, // FUTURE could keep Amount type and add custom jackson serializer
)
