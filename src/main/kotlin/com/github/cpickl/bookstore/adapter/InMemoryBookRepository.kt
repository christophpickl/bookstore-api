package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookRepository
import com.github.cpickl.bookstore.domain.Id
import mu.KotlinLogging.logger
import org.springframework.stereotype.Repository
import java.lang.IllegalArgumentException

@Repository
class InMemoryBookRepository : BookRepository {

    private val log = logger {}
    private val books = mutableListOf<Book>()

    override fun findAll() = books

    override fun findOrNull(id: Id) =
        books.firstOrNull { it.id == id }

    override fun create(book: Book) {
        log.debug { "create: $book" }
        require(findOrNull(book.id) == null)
        books += book
    }

    override fun update(book: Book) {
        log.debug { "update: $book" }
        val found = findOrNull(book.id) ?: throw IllegalArgumentException("Book not found: ${book.id}")
        require(books.remove(found))
        books += book
    }

    fun clear() {
        log.info { "clear() ... for TEST only!" }
        books.clear()
    }
}
