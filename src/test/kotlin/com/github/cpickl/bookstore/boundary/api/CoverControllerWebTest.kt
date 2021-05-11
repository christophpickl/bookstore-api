package com.github.cpickl.bookstore.boundary.api

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.github.cpickl.bookstore.TestUserPreparer
import com.github.cpickl.bookstore.boundary.Jwt
import com.github.cpickl.bookstore.boundary.NamedByteArrayResource
import com.github.cpickl.bookstore.boundary.isError
import com.github.cpickl.bookstore.boundary.login
import com.github.cpickl.bookstore.boundary.uploadEntity
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookNotFoundException
import com.github.cpickl.bookstore.domain.CoverImage
import com.github.cpickl.bookstore.domain.CoverService
import com.github.cpickl.bookstore.domain.CoverUpdateRequest
import com.github.cpickl.bookstore.domain.ErrorCode
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.isForbidden
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.isStatus
import com.github.cpickl.bookstore.requestAny
import com.github.cpickl.bookstore.requestGet
import com.github.cpickl.bookstore.withJwt
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CoverControllerWebTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val userPreparer: TestUserPreparer,
) {

    private val loginDto = userPreparer.userLogin
    private val book = Book.any()
    private val unknownBookId = Id.any()
    private val cover = CoverImage.DefaultImage

    @MockBean
    private lateinit var coverService: CoverService

    @BeforeAll
    fun `init user`() {
        userPreparer.saveTestUser()
    }

    @Nested
    inner class GetCoverTest {
        @Test
        fun `When get unknown cover Then not found`() {
            whenever(coverService.find(unknownBookId)).thenThrow(BookNotFoundException(unknownBookId))

            val response = restTemplate.requestGet("/api/books/$unknownBookId/cover")

            assertThat(response).isError(
                status = 404,
                code = ErrorCode.BOOK_NOT_FOUND,
            )
        }

        @Test
        fun `Given book When get cover Then return image`() {
            whenever(coverService.find(book.id)).thenReturn(cover)

            val response = restTemplate.requestAny<ByteArray>(GET, "/api/books/${book.id}/cover")

            assertThat(response).isOk()
            assertThat(response.headers[CONTENT_TYPE]).isNotNull().containsExactly(IMAGE_PNG_VALUE)
            assertThat(response.body).isNotNull()
            assertThat(response.body.contentEquals(cover.bytes)).isTrue()
        }
    }

    @Nested
    inner class UpdateCoverTest {

        private val updateResource = NamedByteArrayResource("will_be_ignored.png", byteArrayOf(1, 1, 1, 1))
        private val anyResource = updateResource

        @Test
        fun `When update cover without token Then return forbidden`() {
            val response = restTemplate.exchange<String>(
                "/api/books/${book.id}/cover",
                PUT,
                uploadEntity(anyResource, jwt = null)
            )

            assertThat(response).isForbidden()
        }

        @Test
        fun `Given logged-in When update cover for unknown book Then not found`() {
            whenever(coverService.update(book.id, CoverUpdateRequest(updateResource.byteArray)))
                .thenThrow(BookNotFoundException(book.id))
            val jwt = restTemplate.login(loginDto)

            val response =
                restTemplate.exchange<String>("/api/books/${book.id}/cover", PUT, uploadEntity(updateResource, jwt))

            assertThat(response).isError(
                status = 404,
                code = ErrorCode.BOOK_NOT_FOUND,
            )
        }

        @Test
        fun `Given logged-in When update cover Then succeed`() {
            whenever(coverService.update(book.id, CoverUpdateRequest(updateResource.byteArray))).thenReturn(book)
            val jwt = restTemplate.login(loginDto)

            val response =
                restTemplate.exchange<Any>("/api/books/${book.id}/cover", PUT, uploadEntity(updateResource, jwt))

            assertThat(response).isStatus(NO_CONTENT)
        }
    }


    @Nested
    inner class DeleteCoverTest {
        @Test
        fun `When delete cover without token Then return forbidden`() {
            val response = restTemplate.requestDeleteCover(book.id, jwt = null)

            assertThat(response).isForbidden()
        }

        @Test
        fun `Given logged-in and non-existing cover When delete it Then nothing found`() {
            val jwt = restTemplate.login(loginDto)
            whenever(coverService.delete(book.id)).thenThrow(BookNotFoundException(book.id))

            val response = restTemplate.requestDeleteCover(book.id, jwt)

            assertThat(response).isError(
                status = 404,
                code = ErrorCode.BOOK_NOT_FOUND,
            )
        }

        @Test
        fun `Given logged-in and existing cover When delete it Then return no content`() {
            whenever(coverService.delete(book.id)).thenReturn(book)
            val jwt = restTemplate.login(loginDto)

            val response = restTemplate.requestDeleteCover(book.id, jwt)

            assertThat(response).isStatus(NO_CONTENT)
        }

        private fun TestRestTemplate.requestDeleteCover(id: Id, jwt: Jwt?) =
            requestAny<String>(DELETE, "/api/books/$id/cover", headers = HttpHeaders().apply {
                jwt?.let { withJwt(it) }
            })
    }
}
