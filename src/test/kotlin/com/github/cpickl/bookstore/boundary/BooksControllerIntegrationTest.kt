package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.github.cpickl.bookstore.domain.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerIntegrationTests(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val passwordEncoder: BCryptPasswordEncoder,
) {

    @MockBean
    private lateinit var bookService: BookService

    private val book = Book.any()
    private val userLogin = LoginDto.any()
    private val user = User.any().copy(
        username = userLogin.username,
        passwordHash = passwordEncoder.encode(userLogin.password),
    )

    @BeforeAll
    fun `init user`() {
        userRepository.save(user)
    }

    @Nested
    inner class GetBooksTest {
        @Test
        fun `Given book When get all books with any accept Then return that book in JSON by default`() {
            whenever(bookService.getBooks()).thenReturn(listOf(book))

            val response = restTemplate.get("/books", HttpHeaders().apply {
                this[HttpHeaders.ACCEPT] = "*/*"
            })

            assertThat(response).isOk()
            assertThat(response.body).isEqualTo("""[{"id":"${book.id}","title":"${book.title}","author":"${book.authorName}","price":"${book.price.formatted}"}]""")
            assertThat(response.read<List<BookListDto>>()).isEqualTo(listOf(book.toBookListDto()))
        }

        @Test
        fun `Given book When get all books as JSON Then return JSON`() {
            whenever(bookService.getBooks()).thenReturn(listOf(book))

            val response = restTemplate.get("/books", HttpHeaders().apply {
                this[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
            })

            assertThat(response).isOk()
            assertThat(response.body).isEqualTo("""[{"id":"${book.id}","title":"${book.title}","author":"${book.authorName}","price":"${book.price.formatted}"}]""")
        }

        @Test
        fun `Given book When get all books as XML Then return XML`() {
            whenever(bookService.getBooks()).thenReturn(listOf(book))

            val response = restTemplate.get("/books", HttpHeaders().apply {
                this[HttpHeaders.ACCEPT] = MediaType.APPLICATION_XML_VALUE
            })

            assertThat(response).isOk()
            assertThat(response.body).isEqualTo("""<List><item><id>${book.id}</id><title>${book.title}</title><author>${book.authorName}</author><price>${book.price.formatted}</price></item></List>""")
        }
    }

    @Nested
    inner class CreateBookTest {
        private val anyBody = null

        @Test
        fun `When create book without token Then status forbidden`() {
            val response = restTemplate.post("/books", anyBody, HttpHeaders.EMPTY)

            assertThat(response).isForbidden()
        }

        @Test
        fun `Given proper token When create book with token Then created`() {
            val jwt = restTemplate.login(userLogin)
            val requestBody = BookCreateRequestDto.any()
            whenever(
                bookService.createBook(
                    BookCreateRequest(
                        username = userLogin.username,
                        title = requestBody.title,
                        description = requestBody.description,
                        euroCent = requestBody.euroCents,
                    )
                )
            )
                .thenReturn(book)

            val response = restTemplate.post("/books", requestBody, HttpHeaders().apply {
                this[HttpHeaders.AUTHORIZATION] = "Bearer $jwt"
            })

            assertThat(response).isOk()
            assertThat(response.read<BookDetailDto>()).isEqualTo(
                BookDetailDto(
                    id = book.id.toString(),
                    title = book.title,
                    description = book.description,
                    price = book.price.formatted,
                    author = user.authorPseudonym,
                )
            )
        }

        // FUTURE test for bad requests
    }

    private fun Book.toBookListDto() = BookListDto(
        id = id.toString(),
        title = title,
        author = authorName,
        price = price.formatted
    )
}
