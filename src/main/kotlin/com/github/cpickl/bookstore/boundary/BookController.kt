package com.github.cpickl.bookstore.boundary

import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.Search
import com.github.cpickl.bookstore.domain.unaryPlus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_XML_VALUE
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

@Tag(
    name = "Book API",
    description = "CRUD operations for books (partially secured)."
)
@RestController
@RequestMapping(
    "/books",
    produces = [APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE],
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
        BooksDto(service.findAll(searchTerm.toSearch()).map { it.toBookSimpleDto() })


    @Operation(
        operationId = "findBook",
        summary = "find a single book",
        description = "Try to find a single book by it's ID.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "book found by ID"),
            ApiResponse(
                responseCode = "400", description = "invalid content/ID given",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "book not found",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
        ]
    )
    @GetMapping("/{id}")
    fun findSingleBook(
        @PathVariable id: UUID
    ): BookDto =
        service.find(+id).toBookDto()


    @Operation(
        operationId = "createBook",
        summary = "create a new book",
        description = "Define the basic data of a new book.",
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "book successfully created"),
        ApiResponse(
            responseCode = "400", description = "invalid content given",
            content = [Content(schema = Schema(implementation = ErrorDto::class))]
        ),
    ])
    @PostMapping("",
        consumes = [APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE]
    )
    fun createBook(
        @RequestBody book: BookCreateDto,
        auth: Authentication
    ): BookDto =
        service.create(book.toBookCreateRequest(auth.username)).toBookDto()


    @Operation(
        operationId = "updateBook",
        summary = "update an existing book",
        description = "Update the basic data of a yet existing book.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "book successfully updated"),
            ApiResponse(
                responseCode = "400", description = "invalid content given",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "book not found",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
        ]
    )
    @PutMapping(
        "/{id}",
        consumes = [APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE]
    )
    fun updateBook(
        @PathVariable id: UUID,
        @RequestBody update: BookUpdateDto,
        auth: Authentication
    ): BookDto =
        service.update(update.toBookUpdateRequest(auth.username, +id)).toBookDto()


    @Operation(
        operationId = "deleteBook",
        summary = "delete an existing book",
        description = "Simply sets the internal state to 'unpublished', thus filtering it out.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "book successfully deleted"),
            ApiResponse(
                responseCode = "400", description = "invalid content/ID given",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "book not found",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
        ]
    )
    @DeleteMapping("/{id}")
    fun deleteBook(
        @PathVariable id: UUID,
        auth: Authentication,
    ): BookDto =
        service.delete(auth.username, +id).toBookDto()
}

private fun String?.toSearch() = if (this == null) Search.Off else Search.On(this)

private val Authentication.username get() = principal as String
