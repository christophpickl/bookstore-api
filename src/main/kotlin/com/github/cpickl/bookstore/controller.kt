package com.github.cpickl.bookstore

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/books")
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
    price = price.formatted
)

data class BookListDto(
    val id: Int,
    val title: String,
    val author: String,
    val price: String, // FUTURE could keep Amount type and add custom jackson serializer
)
/*
    val description: String,
    val coverImage: Image,
 */
