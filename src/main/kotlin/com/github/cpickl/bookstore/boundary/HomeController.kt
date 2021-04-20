package com.github.cpickl.bookstore.boundary

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Home API",
    description = "Simply provides some further links."
)
@RestController
@RequestMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
class HomeController {
    @GetMapping("")
    fun getHome() = HomeDto()
}

@Schema(
    name = "Home",
)
@Suppress("unused")
@JacksonXmlRootElement(localName = "home")
class HomeDto {
    val selfLink = LinkDto.get("/")
    val loginLink = LinkDto(Method.POST, "/login")
    val booksLink = LinkDto.get("/books")
}
