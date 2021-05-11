package com.github.cpickl.bookstore.boundary.html

import kotlinx.html.BODY
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

fun htmlResponse(content: BODY.() -> Unit): ResponseEntity<String> = ResponseEntity
    .status(HttpStatus.OK)
    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
    .body(htmlDocument(content))

fun htmlDocument(content: BODY.() -> Unit): String = createHTML().html {
    head {
        title {
            +"Bookstore"
        }
        link(rel = "stylesheet", href = "/html/main.css")
    }
    body {
        header()
        content()
        footer()
    }
}

fun BODY.header() {
    p {
        id = "applicationTitle"
        +"Bookstore"
    }
}

fun BODY.footer() {
    hr()
    p {
        +"No copyright."
    }
}
