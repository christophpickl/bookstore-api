package com.github.cpickl.bookstore.boundary

import assertk.Assert
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.cpickl.bookstore.domain.ErrorCode
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
    jwt: Jwt? = null,
    fileType: MediaType = MediaType.IMAGE_PNG,
): HttpEntity<LinkedMultiValueMap<String, Any>> = HttpEntity(
    LinkedMultiValueMap<String, Any>().apply {
        add("cover-file", HttpEntity(bytesResource, HttpHeaders().apply {
            contentType = fileType
        }))
    },
    HttpHeaders().apply {
        contentType = MediaType.MULTIPART_FORM_DATA
        if (jwt != null) {
            withJwt(jwt)
        }
    })


data class Jwt(private val value: String) {
    companion object {
        fun any() = Jwt(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTYxOTQ2MTIyOX0." +
                    "Idh9mm3W_TUfPnUPZh2S6fwnylXWQE33wzPewVct-dm4h61ZXRvHVffBxeIX_VxjF2Su9rHvn-qtmeFVwgmmOw"
        )
    }

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

fun Assert<ResponseEntity<*>>.contentTypeIs(expected: MediaType, exactMatch: Boolean = false) {
    given {
        assertThat(it.headers[HttpHeaders.CONTENT_TYPE]).isNotNull().hasSize(1)
        val given = MediaType.parseMediaType(it.headers[HttpHeaders.CONTENT_TYPE]!!.first())
        if (exactMatch) {
            assertThat(given).isEqualTo(expected)
        } else {
            assertThat("${given.type}/${given.subtype}").isEqualTo(expected.toString())
        }
    }
}

class NamedByteArrayResource(
    private val fileName: String,
    fileContent: ByteArray
) : ByteArrayResource(fileContent) {
    override fun getFilename() = fileName
}

fun Assert<ResponseEntity<String>>.isError(
    messageContains: String? = null,
    status: Int? = null,
    code: ErrorCode? = null
) {
    given { response ->
        status?.let { assertThat(response.statusCodeValue).isEqualTo(it) }
        val dto = jackson.readValue<ErrorDto>(response.body!!)

        messageContains?.let { assertThat(dto.message).contains(it) }
        status?.let { assertThat(dto.status).isEqualTo(it) }
        code?.let { assertThat(dto.code).isEqualTo(it) }
    }
}
