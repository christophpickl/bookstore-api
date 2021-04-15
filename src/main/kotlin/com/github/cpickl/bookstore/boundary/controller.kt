package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BooksService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/books", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
class BooksController(
    private val service: BooksService
) {
    @GetMapping("")
    fun listAllBooks(): List<BookListDto> =
        service.getBooks().map { it.toBookListDto() }

    private fun Book.toBookListDto() = BookListDto(
        id = id,
        title = title,
        author = authorName,
        price = price.formatted // FUTURE render complex Amount object instead
    )
}
