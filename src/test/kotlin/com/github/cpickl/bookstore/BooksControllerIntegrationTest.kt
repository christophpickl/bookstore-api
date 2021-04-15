package com.github.cpickl.bookstore

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.*
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BooksControllerIntegrationTests(
    @Autowired val restTemplate: TestRestTemplate
) {

    @MockBean
    private lateinit var booksService: BooksService
    private val jackson = jacksonObjectMapper()
    private val book = Book.any()

    @Test
    fun `Given book When get all books Then return that book`() {
        whenever(booksService.getBooks()).thenReturn(listOf(book))

        val response = get("/books")

        assertThat(response).isStatusCodeOk()
        assertThat(response.body).isEqualTo("""[{"id":${book.id},"title":"${book.title}","author":"${book.authorName}","price":"${book.price.formatted}"}]""")
        assertThat(response.body).isEqualTo(jackson.writeValueAsString(listOf(book.toBookListDto())))
    }

    @Test
    fun `Given book When get all books as JSON Then return JSON`() {
        whenever(booksService.getBooks()).thenReturn(listOf(book))

        val response = get("/books", HttpHeaders().apply {
            this[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
        })

        assertThat(response).isStatusCodeOk()
        assertThat(response.body).isEqualTo("""[{"id":${book.id},"title":"${book.title}","author":"${book.authorName}","price":"${book.price.formatted}"}]""")
    }

    @Test
    fun `Given book When get all books as XML Then return XML`() {
        whenever(booksService.getBooks()).thenReturn(listOf(book))

        val response = get("/books", HttpHeaders().apply {
            this[HttpHeaders.ACCEPT] = MediaType.APPLICATION_XML_VALUE
        })

        assertThat(response).isStatusCodeOk()
        assertThat(response.body).isEqualTo("""<List><item><id>${book.id}</id><title>${book.title}</title><author>${book.authorName}</author><price>${book.price.formatted}</price></item></List>""")
    }

    private fun get(path: String, headers: HttpHeaders = HttpHeaders()): ResponseEntity<String> =
        restTemplate.exchange(RequestEntity<Any>(headers, HttpMethod.GET, URI(path)))
}

fun Assert<ResponseEntity<*>>.isStatusCodeOk() {
    given {
        assertThat(it.statusCode).isEqualTo(HttpStatus.OK)
    }
}
