package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.requestGet
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HomeControllerWebTest(
    @Autowired private val restTemplate: TestRestTemplate,
) {
    @Test
    fun `When get home without accept header Then default to JSON`() {
        val response = restTemplate.requestGet("/", headers = HttpHeaders().apply {
            remove(HttpHeaders.ACCEPT)
        })

        assertThat(response).isOk()
        assertThat(response).contentTypeIs(MediaType.APPLICATION_JSON)
    }

    @Test
    fun `When get home accepting HTML Then return HTML`() {
        val response = restTemplate.requestGet("/", headers = HttpHeaders().apply {
            set(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
        })

        assertThat(response).isOk()
        assertThat(response).contentTypeIs(MediaType.TEXT_HTML)
    }

    @Test
    fun `When get home accepting JSON Then return JSON`() {
        val response = restTemplate.requestGet("/", headers = HttpHeaders().apply {
            set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        })

        assertThat(response).isOk()
        assertThat(response).contentTypeIs(MediaType.APPLICATION_JSON)
    }
}
