package com.github.cpickl.bookstore.boundary.api

import assertk.assertThat
import com.github.cpickl.bookstore.isStatus
import com.github.cpickl.bookstore.requestGet
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiWebTest(
    @Autowired private val restTemplate: TestRestTemplate,
) {

    @Test
    fun `When get api docs Then return ok`() {
        val response = restTemplate.requestGet("/v3/api-docs")

        assertThat(response).isStatus(HttpStatus.OK)
    }

    @Test
    fun `When get api yaml Then return ok`() {
        val response = restTemplate.requestGet("/v3/api-docs.yaml")

        assertThat(response).isStatus(HttpStatus.OK)
    }
    @Test
    fun `When get swagger html Then return found`() {
        val response = restTemplate.requestGet("/swagger-ui.html")

        assertThat(response).isStatus(HttpStatus.FOUND)
    }
}
