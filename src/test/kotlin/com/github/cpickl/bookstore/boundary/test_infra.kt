package com.github.cpickl.bookstore.boundary

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.github.cpickl.bookstore.isOk
import com.github.cpickl.bookstore.jackson
import com.github.cpickl.bookstore.readAuthorization
import com.github.cpickl.bookstore.requestPost
import com.github.cpickl.bookstore.withJwt
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.xmlunit.builder.DiffBuilder

fun TestRestTemplate.login(dto: LoginDto): Jwt {
    val response = requestPost("/login", jackson.writeValueAsString(dto), HttpHeaders().apply {
        this[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
    })

    assertThat(response).isOk()
    return Jwt(response.headers.readAuthorization())
}

fun uploadEntity(
    bytesResource: ByteArrayResource,
    jwt: Jwt? = null
): HttpEntity<LinkedMultiValueMap<String, Any>> = HttpEntity(
    LinkedMultiValueMap<String, Any>().apply {
        add("cover-file", HttpEntity(bytesResource, HttpHeaders().apply {
            contentType = MediaType.IMAGE_PNG
        }))
    },
    HttpHeaders().apply {
        contentType = MediaType.MULTIPART_FORM_DATA
        if (jwt != null) {
            withJwt(jwt)
        }
    })


class Jwt(private val value: String) {
    override fun toString() = value
}

fun Assert<ResponseEntity<String>>.bodyIsEqualJson(expectedJson: String, strict: Boolean = true) {
    given {
        JSONAssert.assertEquals(expectedJson, it.body!!, strict)
    }
}

fun Assert<ResponseEntity<String>>.bodyIsEqualXml(expectedXml: String) {
    given {
        val diff = DiffBuilder.compare(it.body!!)
            .ignoreWhitespace()
            .withTest(expectedXml)
            .build()
//        if(diff.hasDifferences()) {
//            println("Expected:{{$expectedXml}}")
//            println("Actual:{{${it.body}}}")
//        }
        assertThat(diff.differences).isEmpty()
    }
}

fun Assert<ResponseEntity<*>>.contentTypeIs(type: MediaType) {
    given {
        assertThat(it.headers[HttpHeaders.CONTENT_TYPE]).isEqualTo(listOf(type.toString()))
    }
}

class NamedByteArrayResource(
    private val fileName: String,
    fileContent: ByteArray
) : ByteArrayResource(fileContent) {
    override fun getFilename() = fileName
}
