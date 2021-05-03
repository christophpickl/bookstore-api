package com.github.cpickl.bookstore.boundary

import assertk.assertThat
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.requestGet
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HomeControllerApiTest(
    @Autowired private val restTemplate: TestRestTemplate,
) {
    @Test
    fun `When get home Then return 200 OK`() {
        val response = restTemplate.requestGet("/api")

        assertThat(response).isOk()
    }
}
