package com.github.cpickl.bookstore.boundary

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BooksService
import com.github.cpickl.bookstore.domain.any
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BooksControllerIntegrationTests(
    @Autowired val restTemplate: TestRestTemplate
) {

    @MockBean
    private lateinit var booksService: BooksService
    private val jackson = jacksonObjectMapper()
    private val book = Book.any()

    @Nested
    inner class GetBooksTest {
        @Test
        fun `Given book When get all books with any accept Then return that book in JSON by default`() {
            whenever(booksService.getBooks()).thenReturn(listOf(book))

            val response = restTemplate.get("/books", HttpHeaders().apply {
                this[HttpHeaders.ACCEPT] = "*/*"
            })

            assertThat(response).isStatusCodeOk()
            assertThat(response.body).isEqualTo("""[{"id":${book.id},"title":"${book.title}","author":"${book.authorName}","price":"${book.price.formatted}"}]""")
            assertThat(response.body).isEqualTo(jackson.writeValueAsString(listOf(book.toBookListDto())))
        }

        @Test
        fun `Given book When get all books as JSON Then return JSON`() {
            whenever(booksService.getBooks()).thenReturn(listOf(book))

            val response = restTemplate.get("/books", HttpHeaders().apply {
                this[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
            })

            assertThat(response).isStatusCodeOk()
            assertThat(response.body).isEqualTo("""[{"id":${book.id},"title":"${book.title}","author":"${book.authorName}","price":"${book.price.formatted}"}]""")
        }

        @Test
        fun `Given book When get all books as XML Then return XML`() {
            whenever(booksService.getBooks()).thenReturn(listOf(book))

            val response = restTemplate.get("/books", HttpHeaders().apply {
                this[HttpHeaders.ACCEPT] = MediaType.APPLICATION_XML_VALUE
            })

            assertThat(response).isStatusCodeOk()
            assertThat(response.body).isEqualTo("""<List><item><id>${book.id}</id><title>${book.title}</title><author>${book.authorName}</author><price>${book.price.formatted}</price></item></List>""")
        }
    }


    private fun Book.toBookListDto() = BookListDto(
        id = id,
        title = title,
        author = authorName,
        price = price.formatted // FUTURE render complex Amount object instead
    )
}
