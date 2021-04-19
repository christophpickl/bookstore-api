package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.BookUpdateRequest
import com.github.cpickl.bookstore.domain.Currency
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.Search
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/books",
    produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
)
@Tag(
    name = "Book API",
    description = "CRUD operations for books (partially secured)"
)
class BookController(
    private val service: BookService
) {
    @Operation(
        operationId = "listBooks",
        summary = "list all books",
        description = "List or search books by given search term.",
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Some books might be found."),
    ])
    @GetMapping("")
    fun findAllBooks(
        @RequestParam(name = "search", required = false)
        @Parameter(description="Single search term to filter books.")
        searchTerm: String?
    ): BooksDto =
        BooksDto(books = service.findAll(searchTerm.toSearch()).map { it.toBookSimpleDto() })


    @Operation(
        operationId = "findBook",
        summary = "get a single book",
        description = "Try to find a single book by it's ID.",
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "book found by ID"),
        ApiResponse(responseCode = "404", description = "book not found", content = [Content()]),
    ])
    @GetMapping("/{id}")
    fun findSingleBook(
        @PathVariable id: UUID
    ): ResponseEntity<BookDto> =
        service.findOrNull(Id(id))?.let {
            ok(it.toBookDto())
        } ?: ResponseEntity.notFound().build()


    @PostMapping("",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun createBook(
        @RequestBody book: BookCreateDto,
        auth: Authentication
    ): BookDto =
        service.create(book.toBookCreateRequest(auth.username)).toBookDto()


    @PutMapping("/{id}",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun updateBook(
        @PathVariable id: UUID,
        @RequestBody update: BookUpdateDto,
        auth: Authentication
    ): ResponseEntity<BookDto> =
        service.update(BookUpdateRequest(auth.username, Id(id), update))?.let {
            ok(it.toBookDto())
        } ?: ResponseEntity.notFound().build()


    @DeleteMapping("/{id}")
    fun deleteBook(
        @PathVariable id: UUID,
        auth: Authentication
    ): ResponseEntity<BookDto> =
        service.delete(auth.username, Id(id))?.let { book ->
            ok(book.toBookDto())
        } ?: ResponseEntity.notFound().build()
}

private fun String?.toSearch() = if (this == null) Search.Off else Search.On(this)

private val Authentication.username get() = principal as String

private fun Book.toBookSimpleDto() = BookSimpleDto(
    id = id.toString(),
    title = title,
)

private fun BookCreateDto.toBookCreateRequest(username: String) = BookCreateRequest(
    username = username,
    title = title,
    description = description,
    price = price.toMoney(),
)

private fun Book.toBookDto() = BookDto(
    id = id.toString(),
    title = title,
    description = description,
    price = price.toMoneyDto(),
    author = authorName,
)

private fun Money.toMoneyDto() = MoneyDto(
    currencyCode = currency.code,
    value = value,
    precision = currency.precision,
)

private fun MoneyDto.toMoney() = Money(
    currency = Currency.of(currencyCode),
    value = value,
)
