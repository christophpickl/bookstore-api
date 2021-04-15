package com.github.cpickl.bookstore

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BooksController(
    private val service: BooksService
) {
    @GetMapping("/books")
    fun getAllBooks(): List<BookDto> =
        service.getBooks().map { it.toBookDto() }

    private fun Book.toBookDto() = BookDto(
        id = id,
        title = title,
    )
}

data class BookDto(
    val id: Int,
    val title: String,
)
