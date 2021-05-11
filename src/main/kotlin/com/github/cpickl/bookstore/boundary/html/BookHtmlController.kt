package com.github.cpickl.bookstore.boundary.html

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.Id
import kotlinx.html.ATarget
import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.html
import kotlinx.html.li
import kotlinx.html.stream.createHTML
import kotlinx.html.ul
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.annotation.security.PermitAll

@RestController
@RequestMapping("/html", produces = [MediaType.TEXT_HTML_VALUE])
class BookHtmlController(
    private val service: BookService
) {

    @GetMapping("")
    @PermitAll
    fun home() = ResponseEntity.ok(createHTML().body {
        h1 {
            +"Welcome to the Bookstore."
        }
        ul {
            service.findAll().forEach { book ->
                renderBook(book)
            }
        }
    })

    @GetMapping("/book/{id}")
    @PermitAll
    fun bookDetail(@PathVariable id: UUID): ResponseEntity<String> {
        val book = service.find(Id(id))
        return ResponseEntity.ok(createHTML().html {
            body {
                h1 {
                    +book.title
                }
                ul {
                    li { +"ID: ${book.id}" }
                    li { +"Description: ${book.description}" }
                }
            }
        })
    }

}

private fun UL.renderBook(book: Book) {
    li {
        a(href = "/html/book/${book.id}", target = ATarget.self) {
            +book.title
        }
    }
}
