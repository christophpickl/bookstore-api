package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.github.cpickl.bookstore.UUID1
import com.github.cpickl.bookstore.UserTestPreparer
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.BookUpdateRequest
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.requestGet
import com.github.cpickl.bookstore.isBadRequest
import com.github.cpickl.bookstore.isForbidden
import com.github.cpickl.bookstore.isNotFound
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.requestPost
import com.github.cpickl.bookstore.requestPut
import com.github.cpickl.bookstore.read
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
    private val invalidBookId = UUID1
    private val loginDto = userPreparer.userLogin

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
            // TODO fine-tune XML node name
            assertThat(response.body).isEqualTo("""<List><item><id>${book.id}</id><title>${book.title}</title><author>${book.authorName}</author><price>${book.price.formatted}</price></item></List>""")
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
        fun `Given proper token When create book with token Then created`() {
            val jwt = restTemplate.login(loginDto)
            val requestBody = BookCreateDto.any()
            whenever(
                bookService.create(
                    BookCreateRequest(
                        username = userPreparer.userLogin.username,
                        title = requestBody.title,
                        description = requestBody.description,
                        euroCent = requestBody.euroCents,
                    )
                )
            )
                .thenReturn(book)

            val response = restTemplate.requestPost("/books", requestBody, HttpHeaders().apply {
                this[HttpHeaders.AUTHORIZATION] = "Bearer $jwt"
            })

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
            val updateDto = BookUpdateDto.any()
            whenever(bookService.update(BookUpdateRequest(loginDto.username, book.id, updateDto.title)))
                .thenReturn(null)

            val response = restTemplate.requestPut("/books/${book.id}", body = updateDto, HttpHeaders().apply {
                this[HttpHeaders.AUTHORIZATION] = "Bearer $jwt"
            })

            assertThat(response).isNotFound()
        }

        @Test
        fun `Given book When update it Then succeed`() {
            val jwt = restTemplate.login(loginDto)
            val updateDto = BookUpdateDto(title = "title2")
            val book2 = book.copy(title = updateDto.title)
            whenever(bookService.update(BookUpdateRequest(loginDto.username, book.id, updateDto.title)))
                .thenReturn(book2)

            val response = restTemplate.requestPut("/books/${book.id}", body = updateDto, HttpHeaders().apply {
                this[HttpHeaders.AUTHORIZATION] = "Bearer $jwt"
            })

            assertThat(response.read<BookDetailDto>()).isEqualTo(book2.toBookDetailDto())
        }
    }

}
