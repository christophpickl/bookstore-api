package com.github.cpickl.bookstore.boundary.api

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.github.cpickl.bookstore.boundary.LinkDto
import com.github.cpickl.bookstore.boundary.Method
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.PermitAll

@Tag(
    name = "Home API",
    description = "Simply provides some further links."
)
@RestController
@RequestMapping("/api", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
class HomeApiController {

    @GetMapping("")
    @PermitAll
    fun getHome() = HomeDto()
}

@Schema(
    name = "Home",
)
@Suppress("unused")
@JacksonXmlRootElement(localName = "home")
class HomeDto {
    val selfLink = LinkDto.get("/api")
    val loginLink = LinkDto(Method.POST, "/api/login")
    val booksLink = LinkDto.get("/api/books{?search}", templated = true)
}
