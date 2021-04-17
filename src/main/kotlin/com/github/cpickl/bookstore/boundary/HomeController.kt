package com.github.cpickl.bookstore.boundary

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
class HomeController {
    @GetMapping("")
    fun getHome() = HomeDto()
}

@Suppress("unused")
@JacksonXmlRootElement(localName = "home")
class HomeDto {
    val selfLink = "/"
    val loginLink = "/login"
    val booksLink = "/books"
}
