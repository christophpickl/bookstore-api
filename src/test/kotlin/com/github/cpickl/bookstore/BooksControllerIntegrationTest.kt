package com.github.cpickl.bookstore

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BooksControllerIntegrationTests(
    @Autowired val restTemplate: TestRestTemplate
) {

    @MockBean
    private lateinit var booksService: BooksService
    private val jackson = jacksonObjectMapper()
    private val book = Book.any()
    private val books = listOf(book)

    @Test
    fun `Given book When get all books Then return that book`() {
        whenever(booksService.getBooks()).thenReturn(books)

        val response = restTemplate.getForEntity<String>("/books")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEqualTo(jackson.writeValueAsString(books))
    }
}
