package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookService
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
class BookController(
    private val service: BookService
) {
    @GetMapping("")
    fun listAllBooks(): List<BookListDto> =
        service.getBooks().map { it.toBookListDto() }

    @PostMapping("")
    fun createBook(@RequestBody book: BookCreateRequestDto, auth: Authentication): BookDetailDto {
        val username = auth.principal as String
        return service.createBook(book.toBookCreateRequest(username)).toBookDetailDto()
    }

    // READ DETAIL (public/anonymous)

    // UPDATE

    // DELETE

    // UNPUBLISH
}

private fun Book.toBookListDto() = BookListDto(
    id = id.toString(),
    title = title,
    author = authorName,
    price = price.formatted // FUTURE render complex Amount object instead
)

private fun BookCreateRequestDto.toBookCreateRequest(username: String) = BookCreateRequest(
    username = username,
    title = title,
    description = description,
    euroCent = euroCents,
)

private fun Book.toBookDetailDto() = BookDetailDto(
    id = id.toString(),
    title = title,
    description = description,
    price = price.formatted,
    author = authorName,
)
