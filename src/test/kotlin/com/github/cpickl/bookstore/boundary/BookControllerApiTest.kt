package com.github.cpickl.bookstore.boundary

import assertk.Assert
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.cpickl.bookstore.TestUserPreparer
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookNotFoundException
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.ErrorCode
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.Search
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.isForbidden
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.jackson
import com.github.cpickl.bookstore.read
import com.github.cpickl.bookstore.requestDelete
import com.github.cpickl.bookstore.requestGet
import com.github.cpickl.bookstore.requestPost
import com.github.cpickl.bookstore.requestPut
import com.github.cpickl.bookstore.withJwt
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.ALL
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.http.ResponseEntity

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val userPreparer: TestUserPreparer,
) {

    @MockBean
    private lateinit var bookService: BookService

    private val book = Book.any()
    private val bookId = book.id
    private val malformedBookId = "malformedBookId"
    private val invalidBookId = Id.any()
    private val loginDto = userPreparer.userLogin
    private val anyBooks = emptyList<Book>()

    @BeforeAll
    fun `init user`() {
        userPreparer.saveTestUser()
    }

    @Nested
    inner class GetBooksTest {
        @Test
        fun `Given book When get all books with any accept Then return that book in JSON by default`() {
            whenever(bookService.findAll()).thenReturn(listOf(book))

            val response = restTemplate.requestGet("/api/books", HttpHeaders().apply {
                accept = listOf(ALL)
            })

            assertThat(response).isOk()
            assertThat(response).contentTypeIs(APPLICATION_JSON)
        }

        @Test
        fun `Given book When get all books as JSON Then return JSON`() {
            whenever(bookService.findAll()).thenReturn(listOf(book))

            val response = restTemplate.requestGet("/api/books", HttpHeaders().apply {
                accept = listOf(APPLICATION_JSON)
            })

            assertThat(response).isOk()
            assertThat(response).contentTypeIs(APPLICATION_JSON)
            assertThat(response).bodyIsEqualJson("""{"books":[${book.toSimpleJson()}]}""")
            assertThat(response.read<BooksDto>()).isEqualTo(BooksDto(listOf(book.toBookSimpleDto())))
        }


        @Test
        fun `Given book When get all books as XML Then return XML`() {
            whenever(bookService.findAll()).thenReturn(listOf(book))

            val response = restTemplate.requestGet("/api/books", HttpHeaders().apply {
                accept = listOf(APPLICATION_XML)
            })

            assertThat(response).isOk()
            assertThat(response).contentTypeIs(APPLICATION_XML)
            assertThat(response).bodyIsEqualXml("""<books>${book.toSimpleXml()}</books>""")
        }

        @Test
        fun `When find all with search term Then search object passed`() {
            val search = Search.On("testSearch")
            whenever(bookService.findAll(search)).thenReturn(anyBooks)
            val response = restTemplate.requestGet("/api/books?search=${search.term}")

            assertThat(response).isOk()
            verify(bookService).findAll(search)
        }

        @Test
        fun `When find all without search term Then no search object passed`() {
            whenever(bookService.findAll(Search.Off)).thenReturn(anyBooks)
            val response = restTemplate.requestGet("/api/books")

            assertThat(response).isOk()
            verify(bookService).findAll(Search.Off)
        }
    }

    @Nested
    inner class GetBookTest {

        @Test
        fun `When get book by malformed ID Then fail`() {
            val response = restTemplate.requestGet("/api/books/$malformedBookId")

            assertThat(response).isError(
                messageContains = "Bad request",
                status = 400,
                code = ErrorCode.INVALID_INPUT,
            )
        }

        @Test
        fun `When get non existing book Then not found`() {
            whenever(bookService.find(invalidBookId)).thenThrow(BookNotFoundException(invalidBookId))

            val response = restTemplate.requestGet("/api/books/$invalidBookId")

            assertThat(response).isError(
                status = 404,
                code = ErrorCode.BOOK_NOT_FOUND,
            )
        }

        @Test
        fun `Given book When get it Then return`() {
            whenever(bookService.find(book.id)).thenReturn(book)

            val response = restTemplate.requestGet("/api/books/${book.id}")

            assertThat(response).isOk()
            assertThat(response.read<BookDto>()).isEqualTo(
                BookDto(
                    id = book.id.toString(),
                    title = book.title,
                    description = book.description,
                    price = book.price.toMoneyDto(),
                    author = book.authorName,
                )
            )
        }

        @Test
        fun `Given book When get book as JSON Then return JSON`() {
            whenever(bookService.find(bookId)).thenReturn(book)

            val response = restTemplate.requestGet("/api/books/$bookId", HttpHeaders().apply {
                accept = listOf(APPLICATION_JSON)
            })

            assertThat(response).isOk()
            assertThat(response).contentTypeIs(APPLICATION_JSON)
            assertThat(response).bodyIsEqualJson(book.toDetailJson())
        }

        @Test
        fun `Given book When get book as XML Then return XML`() {
            whenever(bookService.find(bookId)).thenReturn(book)

            val response = restTemplate.requestGet("/api/books/$bookId", HttpHeaders().apply {
                accept = listOf(APPLICATION_XML)
            })

            assertThat(response).isOk()
            assertThat(response).contentTypeIs(APPLICATION_XML)
            assertThat(response).bodyIsEqualXml(book.toDetailXml())
        }
    }

    @Nested
    inner class CreateBookTest {
        private val anyBody = null

        @Test
        fun `When create book without token Then status forbidden`() {
            val response = restTemplate.requestPost("/api/books", anyBody, HttpHeaders.EMPTY)

            assertThat(response).isForbidden()
        }

        @Test
        fun `Given token When create book Then created`() {
            val jwt = restTemplate.login(loginDto)
            val requestBody = BookCreateDto.any()
            whenever(
                bookService.create(
                    BookCreateRequest(
                        username = userPreparer.userLogin.username,
                        title = requestBody.title,
                        description = requestBody.description,
                        price = requestBody.price.toMoney(),
                    )
                )
            )
                .thenReturn(book)

            val response = restTemplate.requestPost("/api/books", requestBody, HttpHeaders().withJwt(jwt))

            assertThat(response).isOk()
            assertThat(response.read<BookDto>()).isEqualTo(book.toBookDto())
        }
    }

    @Nested
    inner class UpdateBookTest {
        @Test
        fun `When update without token Then fail`() {
            val response = restTemplate.requestPut("/api/books/${book.id}")

            assertThat(response).isForbidden()
        }

        @Test
        fun `When update non existing book Then not found`() {
            val jwt = restTemplate.login(loginDto)
            val update = BookUpdateDto.any()
            whenever(bookService.update(update.toBookUpdateRequest(loginDto.username, book.id)))
                .thenThrow(BookNotFoundException(book.id))

            val response = restTemplate.requestPut("/api/books/${book.id}", body = update, HttpHeaders().withJwt(jwt))

            assertThat(response).isError(
                status = 404,
                code = ErrorCode.BOOK_NOT_FOUND,
            )
        }

        @Test
        fun `Given book When update it Then succeed`() {
            val jwt = restTemplate.login(loginDto)
            val update = BookUpdateDto.any()
            whenever(bookService.update(update.toBookUpdateRequest(loginDto.username, book.id))).thenReturn(book)

            val response = restTemplate.requestPut("/api/books/${book.id}", body = update, HttpHeaders().withJwt(jwt))

            assertThat(response.read<BookDto>()).isEqualTo(book.toBookDto())
        }
    }

    @Nested
    inner class DeleteBookTest {
        @Test
        fun `Given not logged in When delete Then fail`() {
            val response = restTemplate.requestDelete("/api/books/$bookId")

            assertThat(response).isForbidden()
        }

        @Test
        fun `Given token and no book When delete book Then not found`() {
            val jwt = restTemplate.login(loginDto)
            whenever(bookService.delete(loginDto.username, bookId)).thenThrow(BookNotFoundException(bookId))

            val response = restTemplate.requestDelete("/api/books/$bookId", headers = HttpHeaders().withJwt(jwt))

            assertThat(response).isError(
                status = 404,
                code = ErrorCode.BOOK_NOT_FOUND,
            )
            verify(bookService).delete(loginDto.username, bookId)
        }

        @Test
        fun `Given token and book When delete it Then unpublished`() {
            val jwt = restTemplate.login(loginDto)
            whenever(bookService.delete(loginDto.username, bookId)).thenReturn(book)

            val response = restTemplate.requestDelete("/api/books/$bookId", headers = HttpHeaders().withJwt(jwt))

            assertThat(response).isOk()
            assertThat(response.read<BookDto>()).isEqualTo(book.toBookDto())
            verify(bookService).delete(loginDto.username, bookId)
        }
    }
}

fun Assert<ResponseEntity<String>>.isError(
    messageContains: String? = null,
    status: Int? = null,
    code: ErrorCode? = null
) {
    given { response ->
        status?.let { assertThat(response.statusCodeValue).isEqualTo(it) }
        val dto = jackson.readValue<ErrorDto>(response.body!!)

        messageContains?.let { assertThat(dto.message).contains(it) }
        status?.let { assertThat(dto.status).isEqualTo(it) }
        code?.let { assertThat(dto.code).isEqualTo(it) }
    }
}

private fun Book.toSimpleJson() = """{
    "id": "$id",
    "title": "$title",
    "detailLink": { "method": "GET", "path": "/api/books/$id", "templated": false }
}"""

private fun Book.toDetailXml() = """<book>
    <id>$id</id>
    <title><![CDATA[$title]]></title>
    <description><![CDATA[$description]]></description>
    <price>${price.toXml()}</price>
    <author>$authorName</author>
    <coverLink><method>GET</method><path>/api/books/$id/cover</path><templated>false</templated></coverLink>
    <updateLink><method>PUT</method><path>/api/books/$id</path><templated>false</templated></updateLink>
    <deleteLink><method>DELETE</method><path>/api/books/$id</path><templated>false</templated></deleteLink>
</book>"""

private fun Book.toDetailJson() = """{
    "id": "$id",
    "title": "$title",
    "description": "$description",
    "price": ${price.toJson()},
    "author": "$authorName",
    "coverLink": { "method": "GET", "path": "/api/books/$id/cover", "templated": false },
    "updateLink": { "method": "PUT", "path": "/api/books/$id", "templated": false },
    "deleteLink": { "method": "DELETE", "path": "/api/books/$id", "templated": false }
}"""

private fun Book.toSimpleXml() = """<book>
    <id>$id</id>
    <title>$title</title>
    <detailLink><method>GET</method><path>/api/books/$id</path><templated>false</templated></detailLink>
</book>"""

private fun Money.toJson() =
    """{"currencyCode":"${currency.code}","value":$value,"precision":${currency.precision}}"""

private fun Money.toXml() =
    """<currencyCode>${currency.code}</currencyCode>
    <value>$value</value>
    <precision>${currency.precision}</precision>"""
