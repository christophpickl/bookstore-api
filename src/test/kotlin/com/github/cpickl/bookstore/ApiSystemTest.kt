package com.github.cpickl.bookstore

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.github.cpickl.bookstore.boundary.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiSystemTest(
    @Autowired val restTemplate: TestRestTemplate
) {
    @Tag("system")
    @Test
    fun `read book - create one - read again should return created`() {
        assertThat(restTemplate.get("/books").read<List<BookListDto>>()).isEmpty()

        val created = restTemplate.post("/books", BookCreateRequestDto.any()).read<BookDetailDto>()

        assertThat(restTemplate.get("/books").read<List<BookListDto>>()).containsExactly(created)
    }
}
