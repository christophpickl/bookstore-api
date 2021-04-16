package com.github.cpickl.bookstore

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.github.cpickl.bookstore.adapter.InMemoryBookRepository
import com.github.cpickl.bookstore.boundary.BookCreateDto
import com.github.cpickl.bookstore.boundary.BookDetailDto
import com.github.cpickl.bookstore.boundary.BookListDto
import com.github.cpickl.bookstore.boundary.BookUpdateDto
import com.github.cpickl.bookstore.boundary.Jwt
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.boundary.login
import com.github.cpickl.bookstore.boundary.toBookListDto
import com.github.cpickl.bookstore.domain.Id
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationApiTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired private val userPreparer: UserTestPreparer,
    @Autowired private val bookRepository: InMemoryBookRepository,
    ) {

    private val loginDto = userPreparer.userLogin

    @BeforeAll
    fun `init user`() {
        userPreparer.saveTestUser()
    }

    @BeforeEach
    fun `reset books`() {
        bookRepository.clear()
    }

    @Tag("overall")
    @Test
    fun `Given token When create book Then read all and single returns book`() {
        val jwt = restTemplate.login(loginDto)

        val created = postBook(jwt, BookCreateDto.any())

        assertThat(getAllBooks()).containsExactly(created.toBookListDto())
        assertThat(restTemplate.requestGet("/books/${created.id}").read<BookDetailDto>()).isEqualTo(created)
    }

    @Tag("overall")
    @Test
    fun `Given token and created book When update Then return updated book`() {
        val jwt = restTemplate.login(loginDto)
        val created = postBook(jwt, BookCreateDto.any())

        val updateDto = BookUpdateDto(title = "title2")
        putBook(jwt, Id(created.id), updateDto)

        val updated = created.copy(title = updateDto.title)
        assertThat(restTemplate.requestGet("/books/${created.id}").read<BookDetailDto>()).isEqualTo(updated)
    }

    private fun getAllBooks() =
        restTemplate.requestGet("/books").read<List<BookListDto>>()

    private fun postBook(jwt: Jwt, dto: BookCreateDto) =
        restTemplate.requestPost("/books", dto, HttpHeaders().apply {
            this[HttpHeaders.AUTHORIZATION] = "Bearer $jwt"
        }).read<BookDetailDto>()

    private fun putBook(jwt: Jwt, id: Id, dto: BookUpdateDto) =
        restTemplate.requestPut("/books/$id", body = dto, HttpHeaders().apply {
            this[HttpHeaders.AUTHORIZATION] = "Bearer $jwt"
        }).read<BookDetailDto>()
}
