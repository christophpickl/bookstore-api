package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.BookUpdateRequest
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Search
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/books", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
class BookController(
    private val service: BookService
) {
    @GetMapping("")
    fun findAllBooks(
        @RequestParam(name = "search", required = false) searchTerm: String?
    ): List<BookListDto> =
        service.findAll(searchTerm.toSearch()).map { it.toBookListDto() }

    @GetMapping("/{id}")
    fun findSingleBook(
        @PathVariable id: UUID
    ): ResponseEntity<BookDetailDto> =
        service.findOrNull(Id(id))?.let {
            ResponseEntity.ok(it.toBookDetailDto())
        } ?: ResponseEntity.notFound().build()

    @PostMapping("")
    fun createBook(
        @RequestBody book: BookCreateDto,
        auth: Authentication
    ): BookDetailDto =
        service.create(book.toBookCreateRequest(auth.username)).toBookDetailDto()

    @PutMapping("/{id}")
    fun updateBook(
        @PathVariable id: UUID,
        @RequestBody update: BookUpdateDto,
        auth: Authentication
    ): ResponseEntity<BookDetailDto> =
        service.update(BookUpdateRequest(auth.username, Id(id), update))?.let {
            ResponseEntity.ok(it.toBookDetailDto())
        } ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    fun deleteBook(
        @PathVariable id: UUID,
        auth: Authentication
    ): ResponseEntity<BookDetailDto> =
        service.delete(auth.username, Id(id))?.let { book ->
            ResponseEntity.ok(book.toBookDetailDto())
        } ?: ResponseEntity.notFound().build()
}

private fun String?.toSearch() = if (this == null) Search.Off else Search.On(this)

private val Authentication.username get() = principal as String

private fun Book.toBookListDto() = BookListDto(
    id = id.toString(),
    title = title,
    author = authorName,
    price = price.formatted // FUTURE render complex Amount object instead
)

private fun BookCreateDto.toBookCreateRequest(username: String) = BookCreateRequest(
    username = username,
    title = title,
    description = description,
    euroCent = euroCent,
)

private fun Book.toBookDetailDto() = BookDetailDto(
    id = id.toString(),
    title = title,
    description = description,
    price = price.formatted,
    author = authorName,
)
