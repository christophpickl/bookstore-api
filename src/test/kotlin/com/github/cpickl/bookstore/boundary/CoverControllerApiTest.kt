package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.github.cpickl.bookstore.UserTestPreparer
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.isNotFound
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.requestAny
import com.github.cpickl.bookstore.requestGet
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CoverControllerApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
) {

    private val book = Book.any()
    private val invalidBookId = UUID.randomUUID()

    @MockBean
    private lateinit var bookService: BookService


    @Nested
    inner class GetCoverTest {
        @Test
        fun `When get unknown cover Then not found`() {
            val response = restTemplate.requestGet("/books/$invalidBookId/cover")

            assertThat(response).isNotFound()
        }

        @Test
        fun `Given book When get cover Then return image`() {
            whenever(bookService.findOrNull(book.id)).thenReturn(book)

            val response = restTemplate.requestAny<ByteArray>(HttpMethod.GET, "/books/${book.id}/cover")

            assertThat(response).isOk()
            assertThat(response.headers[HttpHeaders.CONTENT_TYPE]).isNotNull().containsExactly(MediaType.IMAGE_PNG_VALUE)
            assertThat(response.body).isNotNull()
            assertThat(response.body.contentEquals(book.cover.bytes)).isTrue()
        }
    }
}