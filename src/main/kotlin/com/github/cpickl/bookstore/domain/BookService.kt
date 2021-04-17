package com.github.cpickl.bookstore.domain

import mu.KotlinLogging.logger
import org.springframework.stereotype.Service

interface BookService {
    fun findAll(search: Search = Search.Off): List<Book>
    fun findOrNull(id: Id): Book?
    fun create(request: BookCreateRequest): Book
    fun update(request: BookUpdateRequest): Book?
    fun delete(username: String, id: Id): Book?
}

@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val idGenerator: IdGenerator,
) : BookService {

    private val log = logger {}

    override fun findAll(search: Search) = bookRepository.findAll(search)

    override fun findOrNull(id: Id) = bookRepository.findOrNull(id)

    override fun create(request: BookCreateRequest): Book {
        log.info { "create: $request" }
        val user = userRepository.findOrNull(request.username)
            ?: throw IllegalArgumentException("User not found: '${request.username}'")
        val book = Book(
            id = idGenerator.generate(),
            title = request.title,
            description = request.description,
            author = user,
            price = request.price,
            state = BookState.Published,
        )
        bookRepository.create(book)
        return book
    }

    override fun update(request: BookUpdateRequest): Book? {
        log.info { "update: $request" }
        val found = bookRepository.findOrNull(request.id) ?: return null
        // if(found.author.username != request.username) // FUTURE hardening necessary?!

        val updated = found.updateBy(request)
        bookRepository.update(updated)
        return updated
    }

    override fun delete(username: String, id: Id): Book? {
        log.info { "delete: $id" }
        val book = bookRepository.findOrNull(id) ?: return null
        require(book.state != BookState.Unpublished)
        // if(book.author.username != username) // FUTURE hardening necessary?!

        val deleted = book.copy(state = BookState.Unpublished)
        bookRepository.update(deleted)
        return deleted
    }
}

private fun Book.updateBy(update: BookUpdateRequest) = copy(
    title = update.title,
    description = update.description,
    price = update.price,
)
