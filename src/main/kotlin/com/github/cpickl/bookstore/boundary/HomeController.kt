package com.github.cpickl.bookstore.boundary

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.github.cpickl.bookstore.boundary.html.htmlResponse
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.html.a
import kotlinx.html.h1
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {

    @GetMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun homeForJson() = ResponseEntity
        .status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(HomeDto())

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    fun homeForHtml() = htmlResponse {
        h1 {
            +"Home sweet home!"
        }
        a("/html") {
            +"enter"
        }
    }
}

@Schema(
    name = "Home",
)
@Suppress("unused")
@JacksonXmlRootElement(localName = "home")
class HomeDto {
    val selfLink = LinkDto.get("/")
    val entryLink = LinkDto.get("/api")
}
