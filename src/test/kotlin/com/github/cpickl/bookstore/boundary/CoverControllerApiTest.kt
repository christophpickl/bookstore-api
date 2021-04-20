package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.github.cpickl.bookstore.UserTestPreparer
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.CoverImage
import com.github.cpickl.bookstore.domain.CoverService
import com.github.cpickl.bookstore.domain.CoverUpdateRequest
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.isForbidden
import com.github.cpickl.bookstore.isNotFound
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.isStatus
import com.github.cpickl.bookstore.requestAny
import com.github.cpickl.bookstore.requestGet
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.IMAGE_PNG_VALUE


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CoverControllerApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val userPreparer: UserTestPreparer,
) {

    private val loginDto = userPreparer.userLogin
    private val book = Book.any()
    private val anyBookId = Id.any()
    private val unknownBookId = anyBookId
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
            whenever(coverService.find(unknownBookId)).thenReturn(null)

            val response = restTemplate.requestGet("/books/$unknownBookId/cover")

            assertThat(response).isNotFound()
        }

        @Test
        fun `Given book When get cover Then return image`() {
            whenever(coverService.find(book.id)).thenReturn(cover)

            val response = restTemplate.requestAny<ByteArray>(GET, "/books/${book.id}/cover")

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
        fun `When update without token Then return forbidden`() {
            val response = restTemplate.exchange<Any>(
                "/books/${book.id}/cover",
                POST,
                buildUploadEntity(anyResource, jwt = null)
            )

            assertThat(response).isForbidden()
        }

        @Test
        fun `Given token When update unknown cover Then not found`() {
            whenever(coverService.update(book.id, CoverUpdateRequest(updateResource.byteArray))).thenReturn(null)
            val jwt = restTemplate.login(loginDto)

            val response =
                restTemplate.exchange<Any>("/books/${book.id}/cover", POST, buildUploadEntity(updateResource, jwt))

            assertThat(response).isNotFound()
        }

        @Test
        fun `Given token When update cover Then succeed`() {
            whenever(coverService.update(book.id, CoverUpdateRequest(updateResource.byteArray))).thenReturn(book)
            val jwt = restTemplate.login(loginDto)

            val response =
                restTemplate.exchange<Any>("/books/${book.id}/cover", POST, buildUploadEntity(updateResource, jwt))

            assertThat(response).isStatus(HttpStatus.NO_CONTENT)
        }


    }
}
