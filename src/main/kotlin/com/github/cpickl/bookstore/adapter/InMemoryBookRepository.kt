package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.*
import mu.KotlinLogging
import mu.KotlinLogging.logger
import org.springframework.stereotype.Repository

@Repository
class InMemoryBookRepository : BookRepository {

    private val log = logger {}
    private val books = mutableListOf<Book>()

    override fun findAll() = books

    override fun save(book: Book) {
        log.debug { "save: $book" }
        books += book
    }
}
