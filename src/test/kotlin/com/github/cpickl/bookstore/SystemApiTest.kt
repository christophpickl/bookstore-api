package com.github.cpickl.bookstore

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SystemApiTest(
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

    @Tag("system")
    @Test
    fun `Given token When create book Then read all and single returns book`() {
        val jwt = restTemplate.login(loginDto)

        val created = postBookDto(jwt, BookCreateDto.any())

        assertThat(getBooksDto()).containsExactly(created.toBookListDto())
        assertThat(getBookDto(created.id)).isEqualTo(created)
    }

    @Tag("system")
    @Test
    fun `Given token and created book When update Then return updated book`() {
        val jwt = restTemplate.login(loginDto)
        val created = postBookDto(jwt, BookCreateDto.any())

        val updateDto = BookUpdateDto(title = "title2")
        putBookDto(jwt, created.id, updateDto)

        val updated = created.copy(title = updateDto.title)
        assertThat(restTemplate.requestGet("/books/${created.id}").read<BookDetailDto>()).isEqualTo(updated)
    }

    @Tag("system")
    @Test
    fun `Given token and two books When search Then return proper book`() {
        val jwt = restTemplate.login(loginDto)
        postBookDto(jwt, BookCreateDto.any().copy(title = "a"))
        postBookDto(jwt, BookCreateDto.any().copy(title = "b"))

        assertThat(getBooksDto(search = "a").map { it.title }).containsExactly("a")
    }

    @Tag("system")
    @Test
    fun `Given token and deleted book When get Then return empty`() {
        val jwt = restTemplate.login(loginDto)
        val created = postBookDto(jwt, BookCreateDto.any())
        deleteBook(jwt, created.id)

        assertThat(getBooksDto()).isEmpty()
        assertThat(getBook(created.id)).isNotFound()
    }

    private fun getBook(id: String) =
        restTemplate.requestGet("/books/$id")

    private fun getBookDto(id: String) =
        getBook(id).read<BookDetailDto>()

    private fun getBooksDto(search: String? = null) =
        restTemplate.requestGet("/books${search.buildQuery()}").read<List<BookListDto>>()

    private fun postBookDto(jwt: Jwt, dto: BookCreateDto) =
        restTemplate.requestPost("/books", dto, headers = HttpHeaders().withJwt(jwt)).read<BookDetailDto>()

    private fun putBookDto(jwt: Jwt, id: String, dto: BookUpdateDto) =
        restTemplate.requestPut("/books/$id", body = dto, headers = HttpHeaders().withJwt(jwt)).read<BookDetailDto>()

    private fun deleteBook(jwt: Jwt, id: String) =
        restTemplate.requestDelete("/books/$id", headers = HttpHeaders().withJwt(jwt))

    private fun String?.buildQuery() = if (this == null) "" else {
        "?search=${this}"
    }
}
