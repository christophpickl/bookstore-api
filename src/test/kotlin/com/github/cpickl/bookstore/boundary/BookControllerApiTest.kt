package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.github.cpickl.bookstore.UUID1
import com.github.cpickl.bookstore.UserTestPreparer
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.BookUpdateRequest
import com.github.cpickl.bookstore.domain.Search
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.requestGet
import com.github.cpickl.bookstore.isBadRequest
import com.github.cpickl.bookstore.isForbidden
import com.github.cpickl.bookstore.isNotFound
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.requestPost
import com.github.cpickl.bookstore.requestPut
import com.github.cpickl.bookstore.read
import com.github.cpickl.bookstore.requestDelete
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
import org.springframework.http.MediaType

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val userPreparer: UserTestPreparer,
) {

    @MockBean
    private lateinit var bookService: BookService

    private val book = Book.any()
    private val bookId = book.id
    private val invalidBookId = UUID1
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

            val response = restTemplate.requestGet("/books", HttpHeaders().apply {
                this[HttpHeaders.ACCEPT] = "*/*"
            })

            assertThat(response).isOk()
            assertThat(response.body).isEqualTo("""[{"id":"${book.id}","title":"${book.title}","author":"${book.authorName}","price":"${book.price.formatted}"}]""")
            assertThat(response.read<List<BookListDto>>()).isEqualTo(listOf(book.toBookListDto()))
        }

        @Test
        fun `Given book When get all books as JSON Then return JSON`() {
            whenever(bookService.findAll()).thenReturn(listOf(book))

            val response = restTemplate.requestGet("/books", HttpHeaders().apply {
                this[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
            })

            assertThat(response).isOk()
            assertThat(response.body).isEqualTo("""[{"id":"${book.id}","title":"${book.title}","author":"${book.authorName}","price":"${book.price.formatted}"}]""")
        }

        @Test
        fun `Given book When get all books as XML Then return XML`() {
            whenever(bookService.findAll()).thenReturn(listOf(book))

            val response = restTemplate.requestGet("/books", HttpHeaders().apply {
                this[HttpHeaders.ACCEPT] = MediaType.APPLICATION_XML_VALUE
            })

            assertThat(response).isOk()
            assertThat(response.body).isEqualTo("""<List><item><id>${book.id}</id><title>${book.title}</title><author>${book.authorName}</author><price>${book.price.formatted}</price></item></List>""")
        }

        @Test
        fun `When find all with search term Then search object passed`() {
            val search = Search.On("testSearch")
            whenever(bookService.findAll(search)).thenReturn(anyBooks)
            val response = restTemplate.requestGet("/books?search=${search.term}")

            assertThat(response).isOk()
            verify(bookService).findAll(search)
        }

        @Test
        fun `When find all without search term Then no search object passed`() {
            whenever(bookService.findAll(Search.Off)).thenReturn(anyBooks)
            val response = restTemplate.requestGet("/books")

            assertThat(response).isOk()
            verify(bookService).findAll(Search.Off)
        }
    }

    @Nested
    inner class GetBookTest {

        @Test
        fun `When get book by malformed ID Then fail`() {
            val response = restTemplate.requestGet("/books/malformed")

            assertThat(response).isBadRequest()
        }

        @Test
        fun `When get non existing book Then not found`() {
            val response = restTemplate.requestGet("/books/$invalidBookId")

            assertThat(response).isNotFound()
        }

        @Test
        fun `Given book When get it Then return`() {
            whenever(bookService.findOrNull(book.id)).thenReturn(book)

            val response = restTemplate.requestGet("/books/${book.id}")

            assertThat(response).isOk()
            assertThat(response.read<BookDetailDto>()).isEqualTo(
                BookDetailDto(
                    id = book.id.toString(),
                    title = book.title,
                    description = book.description,
                    price = book.price.formatted,
                    author = book.authorName,
                )
            )
        }
    }

    @Nested
    inner class CreateBookTest {
        private val anyBody = null

        @Test
        fun `When create book without token Then status forbidden`() {
            val response = restTemplate.requestPost("/books", anyBody, HttpHeaders.EMPTY)

            assertThat(response).isForbidden()
        }

        @Test
        fun `Given token When create book with token Then created`() {
            val jwt = restTemplate.login(loginDto)
            val requestBody = BookCreateDto.any()
            whenever(
                bookService.create(
                    BookCreateRequest(
                        username = userPreparer.userLogin.username,
                        title = requestBody.title,
                        description = requestBody.description,
                        euroCent = requestBody.euroCent,
                    )
                )
            )
                .thenReturn(book)

            val response = restTemplate.requestPost("/books", requestBody, HttpHeaders().withJwt(jwt))

            assertThat(response).isOk()
            assertThat(response.read<BookDetailDto>()).isEqualTo(
                BookDetailDto(
                    id = book.id.toString(),
                    title = book.title,
                    description = book.description,
                    price = book.price.formatted,
                    author = userPreparer.user.authorPseudonym,
                )
            )
        }

        // FUTURE test for bad requests
    }

    @Nested
    inner class UpdateBookTest {
        @Test
        fun `When update without token Then fail`() {
            val response = restTemplate.requestPut("/books/${book.id}")

            assertThat(response).isForbidden()
        }

        @Test
        fun `When update non existing book Then not found`() {
            val jwt = restTemplate.login(loginDto)
            val update = BookUpdateDto.any()
            whenever(bookService.update(BookUpdateRequest(loginDto.username, book.id, update))).thenReturn(null)

            val response = restTemplate.requestPut("/books/${book.id}", body = update, HttpHeaders().withJwt(jwt))

            assertThat(response).isNotFound()
        }

        @Test
        fun `Given book When update it Then succeed`() {
            val jwt = restTemplate.login(loginDto)
            val update = BookUpdateDto.any()
            whenever(bookService.update(BookUpdateRequest(loginDto.username, book.id, update))).thenReturn(book)

            val response = restTemplate.requestPut("/books/${book.id}", body = update, HttpHeaders().withJwt(jwt))

            assertThat(response.read<BookDetailDto>()).isEqualTo(book.toBookDetailDto())
        }
    }

    @Nested
    inner class DeleteBookTest {
        @Test
        fun `Given not logged in When delete Then fail`() {
            val response = restTemplate.requestDelete("/books/$bookId")

            assertThat(response).isForbidden()
        }
        @Test
        fun `Given token and no book When delete book Then not found`() {
            val jwt = restTemplate.login(loginDto)
            whenever(bookService.delete(loginDto.username, bookId)).thenReturn(null)

            val response = restTemplate.requestDelete("/books/$bookId", headers = HttpHeaders().withJwt(jwt))

            assertThat(response).isNotFound()
            verify(bookService).delete(loginDto.username, bookId)
        }

        @Test
        fun `Given token and book When delete it Then unpublished`() {
            val jwt = restTemplate.login(loginDto)
            whenever(bookService.delete(loginDto.username, bookId)).thenReturn(book)

            val response = restTemplate.requestDelete("/books/$bookId", headers = HttpHeaders().withJwt(jwt))

            assertThat(response).isOk()
            assertThat(response.read<BookDetailDto>()).isEqualTo(book.toBookDetailDto())
            verify(bookService).delete(loginDto.username, bookId)
        }
    }
}
