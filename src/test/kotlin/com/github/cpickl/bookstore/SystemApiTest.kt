package com.github.cpickl.bookstore

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.github.cpickl.bookstore.boundary.BookCreateDto
import com.github.cpickl.bookstore.boundary.BookDto
import com.github.cpickl.bookstore.boundary.BookUpdateDto
import com.github.cpickl.bookstore.boundary.BooksDto
import com.github.cpickl.bookstore.boundary.Jwt
import com.github.cpickl.bookstore.boundary.NamedByteArrayResource
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.boundary.login
import com.github.cpickl.bookstore.boundary.toBookSimpleDto
import com.github.cpickl.bookstore.boundary.uploadEntity
import com.github.cpickl.bookstore.domain.CoverImage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.NO_CONTENT

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SystemApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val userPreparer: TestUserPreparer,
    @Autowired private val repoCleaner: TestRepositoryCleaner,
) {

    private val loginDto = userPreparer.userLogin

    @BeforeEach
    fun `reset data`() {
        repoCleaner.deleteAllEntities()
        userPreparer.saveTestUser()
    }

    @Nested
    inner class BookCrudTest {
        @Test
        fun `Given logged-in When create a new book Then get operations return that book`() {
            val jwt = restTemplate.login(loginDto)

            val created = postBookDto(jwt, BookCreateDto.any())

            assertThat(getBooksDto().books).containsExactly(created.toBookSimpleDto())
            assertThat(getBookDto(created.id)).isEqualTo(created)
            assertThat(getBookCover(created.id).body.contentEquals(CoverImage.DefaultImage.bytes)).isTrue()
        }

        @Test
        fun `Given logged-in and book When update book Then get returns updated version`() {
            val jwt = restTemplate.login(loginDto)
            val created = postBookDto(jwt, BookCreateDto.any())

            val update = BookUpdateDto.any()
            putBookDto(jwt, created.id, update)

            val recentBook = restTemplate.requestGet("/api/books/${created.id}").read<BookDto>()
            assertThat(recentBook.title).isEqualTo(update.title) // only check for title for simplicity sake
        }

        @Test
        fun `Given logged-in and two books When search Then return proper book`() {
            val jwt = restTemplate.login(loginDto)
            postBookDto(jwt, BookCreateDto.any().copy(title = "a"))
            postBookDto(jwt, BookCreateDto.any().copy(title = "b"))

            assertThat(getBooksDto(search = "a").books.map { it.title }).containsExactly("a")
        }

        @Test
        fun `Given logged-in and book When delete book Then return nothing anymore`() {
            val jwt = restTemplate.login(loginDto)
            val created = postBookDto(jwt, BookCreateDto.any())

            deleteBook(jwt, created.id)

            assertThat(getBooksDto().books).isEmpty()
            assertThat(getBook(created.id)).isNotFound()
        }
    }

    @Nested
    inner class CoverTest {

        private val coverBytes = byteArrayOf(1, 0, 1, 0)

        @Test
        fun `Given logged-in and book When update cover Then get returns updated image`() {
            val jwt = restTemplate.login(loginDto)
            val bookId = postBookDto(jwt, BookCreateDto.any()).id

            val updated = updateCover(
                bookId,
                uploadEntity(NamedByteArrayResource("ignored.png", coverBytes), jwt)
            )
            assertThat(updated).isStatus(NO_CONTENT)

            val response = getBookCover(bookId)
            assertThat(response).isOk()
            assertThat(response.body.contentEquals(coverBytes)).isTrue()
        }

        @Test
        fun `Given logged-in and book and cover When delete cover Then get returns default image`() {
            val jwt = restTemplate.login(loginDto)
            val bookId = postBookDto(jwt, BookCreateDto.any()).id
            updateCover(bookId, uploadEntity(NamedByteArrayResource("ignored.png", coverBytes), jwt))

            val deleted = deleteCover(jwt, bookId)
            assertThat(deleted).isStatus(NO_CONTENT)

            val response = getBookCover(bookId)
            assertThat(response).isOk()
            assertThat(response.body.contentEquals(CoverImage.DefaultImage.bytes)).isTrue()
        }
    }

    private fun getBook(id: String) =
        restTemplate.requestGet("/api/books/$id")

    private fun getBookDto(id: String) =
        getBook(id).read<BookDto>()

    private fun getBookCover(id: String) =
        restTemplate.requestAny<ByteArray>(GET, "/api/books/$id/cover")

    private fun getBooksDto(search: String? = null) =
        restTemplate.requestGet("/api/books${search.buildQuery()}").read<BooksDto>()

    private fun postBookDto(jwt: Jwt, dto: BookCreateDto) =
        restTemplate.requestPost("/api/books", dto, headers = HttpHeaders().withJwt(jwt)).read<BookDto>()

    private fun putBookDto(jwt: Jwt, id: String, dto: BookUpdateDto) =
        restTemplate.requestPut("/api/books/$id", body = dto, headers = HttpHeaders().withJwt(jwt)).read<BookDto>()

    private fun deleteBook(jwt: Jwt, id: String) =
        restTemplate.requestDelete("/api/books/$id", headers = HttpHeaders().withJwt(jwt))

    private fun updateCover(id: String, requestEntity: HttpEntity<*>) =
        restTemplate.exchange<Any>("/api/books/$id/cover", HttpMethod.PUT, requestEntity)

    private fun deleteCover(jwt: Jwt, id: String) =
        restTemplate.requestDelete("/api/books/$id/cover", headers = HttpHeaders().withJwt(jwt))

    private fun String?.buildQuery() = if (this == null) "" else {
        "?search=${this}"
    }
}
