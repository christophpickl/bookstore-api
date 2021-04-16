package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.BookUpdateRequest
import com.github.cpickl.bookstore.domain.Id
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/books", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
class BookController(
    private val service: BookService
) {
    @GetMapping("")
    fun findAllBooks(): List<BookListDto> =
        service.findAll().map { it.toBookListDto() }

    @GetMapping("/{id}")
    fun findSingleBook(
        @PathVariable id: UUID
    ): ResponseEntity<BookDetailDto> =
        service.findOrNull(Id(id))?.let {
            ResponseEntity.ok(it.toBookDetailDto())
        } ?: ResponseEntity.notFound().build()

    @PostMapping("")
    fun createBook(@RequestBody book: BookCreateDto, auth: Authentication): BookDetailDto {
        val username = auth.principal as String
        return service.create(book.toBookCreateRequest(username)).toBookDetailDto()
    }

    @PutMapping("/{id}")
    fun updateBook(
        @PathVariable id: UUID,
        @RequestBody update: BookUpdateDto,
        auth: Authentication
    ): ResponseEntity<BookDetailDto> {
        val username = auth.principal as String
        return service.update(BookUpdateRequest(username, Id(id), update.title))?.let {
            ResponseEntity.ok(it.toBookDetailDto())
        } ?: ResponseEntity.notFound().build()
    }

    // DELETE == UNPUBLISH
}

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
    euroCent = euroCents,
)

private fun Book.toBookDetailDto() = BookDetailDto(
    id = id.toString(),
    title = title,
    description = description,
    price = price.formatted,
    author = authorName,
)