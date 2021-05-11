package com.github.cpickl.bookstore.boundary.html

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.Id
import kotlinx.html.ATarget
import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.h1
import kotlinx.html.li
import kotlinx.html.ul
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.annotation.security.PermitAll

@RestController
@RequestMapping("/html", produces = [MediaType.TEXT_HTML_VALUE])
class BookHtmlController(
    private val bookService: BookService
) {

    @GetMapping("")
    @PermitAll
    fun home() = htmlResponse {
        ul {
            bookService.findAll().forEach {
                li(it)
            }
        }
    }

    @GetMapping("/book/{id}")
    @PermitAll
    fun bookDetail(@PathVariable id: UUID) = htmlResponse {
        val book = bookService.find(Id(id))
        h1 {
            +book.title
        }
        ul {
            li { +"ID: ${book.id}" }
            li { +"Description: ${book.description}" }
            li { +"Author: ${book.authorName}" }
        }
    }
}

private fun UL.li(book: Book) {
    li {
        a(href = "/html/book/${book.id}", target = ATarget.self) {
            +book.title
        }
    }
}
