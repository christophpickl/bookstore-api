package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.auth0.jwt.JWT
import com.github.cpickl.bookstore.jackson
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginTest(
    @Autowired val restTemplate: TestRestTemplate
) {

    @Test
    fun `When login as default admin Then success`() {
        val response = login(SecurityConfig.admin)

        assertThat(response).isOk()
        assertThat(response.headers[HttpHeaders.AUTHORIZATION]).isNotNull().isNotEmpty()
        assertThat(JWT.decode(response.headers.readAuthorization()).payload).isNotEmpty()
    }

    @Test
    fun `When login invalid Then fail`() {
        val response = login(LoginDto("admin", ""))

        assertThat(response).isForbidden()
        assertThat(response.headers[HttpHeaders.AUTHORIZATION]).isNull()
    }

    private fun login(dto: LoginDto) =
        restTemplate.post("/login", jackson.writeValueAsString(dto), HttpHeaders().apply {
            this[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
        })
}