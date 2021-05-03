package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.auth0.jwt.JWT
import com.github.cpickl.bookstore.TestUserPreparer
import com.github.cpickl.bookstore.isForbidden
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.jackson
import com.github.cpickl.bookstore.readAuthorization
import com.github.cpickl.bookstore.requestPost
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val userPreparer: TestUserPreparer,
) {

    @BeforeAll
    fun `init user`() {
        userPreparer.saveTestUser()
    }

    @Test
    fun `When login as default admin Then success`() {
        val response = login(userPreparer.userLogin)

        assertThat(response).isOk()
        assertThat(response.headers[AUTHORIZATION]).isNotNull().isNotEmpty()
        assertThat(JWT.decode(response.headers.readAuthorization()).payload).isNotEmpty()
    }

    @Test
    fun `When login invalid Then fail`() {
        val response = login(userPreparer.userLogin.copy(password = "forbidden"))

        assertThat(response).isForbidden()
        assertThat(response.headers[AUTHORIZATION]).isNull()
    }

    private fun login(dto: LoginDto) =
        restTemplate.requestPost("/login", jackson.writeValueAsString(dto), HttpHeaders().apply {
            this[CONTENT_TYPE] = APPLICATION_JSON_VALUE
        })
}
