package com.github.cpickl.bookstore

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.github.cpickl.bookstore.adapter.InMemoryBookRepository
import com.github.cpickl.bookstore.boundary.BookCreateDto
import com.github.cpickl.bookstore.boundary.BookDto
import com.github.cpickl.bookstore.boundary.BookUpdateDto
import com.github.cpickl.bookstore.boundary.BooksDto
import com.github.cpickl.bookstore.boundary.Jwt
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.boundary.login
import com.github.cpickl.bookstore.boundary.toBookSimpleDto
import com.github.cpickl.bookstore.domain.Image
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod

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

    @Test
    fun `Given token When create book Then read all and single returns book`() {
        val jwt = restTemplate.login(loginDto)

        val created = postBookDto(jwt, BookCreateDto.any())

        assertThat(getBooksDto().books).containsExactly(created.toBookSimpleDto())
        assertThat(getBookDto(created.id)).isEqualTo(created)
        assertThat(getBookCover(created.id).body.contentEquals(Image.default)).isTrue()
    }

    @Test
    fun `Given token and book When update Then return updated book`() {
        val jwt = restTemplate.login(loginDto)
        val created = postBookDto(jwt, BookCreateDto.any())

        val update = BookUpdateDto.any()
        putBookDto(jwt, created.id, update)

        val recentBook = restTemplate.requestGet("/books/${created.id}").read<BookDto>()
        assertThat(recentBook.title).isEqualTo(update.title) // only check for title for simplicity
    }

    @Test
    fun `Given token and two books When search Then return proper book`() {
        val jwt = restTemplate.login(loginDto)
        postBookDto(jwt, BookCreateDto.any().copy(title = "a"))
        postBookDto(jwt, BookCreateDto.any().copy(title = "b"))

        assertThat(getBooksDto(search = "a").books.map { it.title }).containsExactly("a")
    }

    @Test
    fun `Given token and deleted book When get Then return empty`() {
        val jwt = restTemplate.login(loginDto)
        val created = postBookDto(jwt, BookCreateDto.any())
        deleteBook(jwt, created.id)

        assertThat(getBooksDto().books).isEmpty()
        assertThat(getBook(created.id)).isNotFound()
    }

    private fun getBook(id: String) =
        restTemplate.requestGet("/books/$id")

    private fun getBookDto(id: String) =
        getBook(id).read<BookDto>()

    private fun getBookCover(id: String) =
        restTemplate.requestAny<ByteArray>(HttpMethod.GET, "/books/$id/cover")

    private fun getBooksDto(search: String? = null) =
        restTemplate.requestGet("/books${search.buildQuery()}").read<BooksDto>()

    private fun postBookDto(jwt: Jwt, dto: BookCreateDto) =
        restTemplate.requestPost("/books", dto, headers = HttpHeaders().withJwt(jwt)).read<BookDto>()

    private fun putBookDto(jwt: Jwt, id: String, dto: BookUpdateDto) =
        restTemplate.requestPut("/books/$id", body = dto, headers = HttpHeaders().withJwt(jwt)).read<BookDto>()

    private fun deleteBook(jwt: Jwt, id: String) =
        restTemplate.requestDelete("/books/$id", headers = HttpHeaders().withJwt(jwt))

    private fun String?.buildQuery() = if (this == null) "" else {
        "?search=${this}"
    }
}
