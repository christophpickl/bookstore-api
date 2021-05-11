package com.github.cpickl.bookstore.boundary.html

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import com.github.cpickl.bookstore.boundary.contentTypeIs
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookNotFoundException
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.isNotFound
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.requestGet
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookHtmlControllerWebTest(
    @Autowired private val restTemplate: TestRestTemplate,
) {

    @MockBean
    private lateinit var bookService: BookService
    private val book = Book.any()

    @Nested
    inner class BookAllTest {
        @Test
        fun `When get home Then return book`() {
            whenever(bookService.findAll()).thenReturn(listOf(book))

            val response = restTemplate.requestGet("/html")

            assertThat(response).isOk()
            assertThat(response).contentTypeIs(MediaType.TEXT_HTML)
            assertThat(response.body).isNotNull().contains(book.id.toString(), book.title)
        }
    }

    @Nested
    inner class BookSingleTest {
        @Test
        fun `Given book When get that book Then return it`() {
            whenever(bookService.find(book.id)).thenReturn(book)

            val response = restTemplate.requestGet("/html/book/${book.id}")

            assertThat(response).isOk()
            assertThat(response).contentTypeIs(MediaType.TEXT_HTML)
            assertThat(response.body).isNotNull().contains(book.title)
        }

        @Test
        fun `Given book not found When get that book Then response with error html`() {
            whenever(bookService.find(book.id)).thenThrow(BookNotFoundException(book.id))

            val response = restTemplate.requestGet("/html/book/${book.id}")

            assertThat(response).isNotFound()
            assertThat(response).contentTypeIs(MediaType.TEXT_HTML)
            assertThat(response.body).isNotNull().contains(book.id.toString())
        }

    }
}
