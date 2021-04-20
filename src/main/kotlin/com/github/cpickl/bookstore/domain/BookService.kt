package com.github.cpickl.bookstore.domain

import mu.KotlinLogging.logger
import org.springframework.stereotype.Service

interface BookService {
    fun findAll(search: Search = Search.Off): List<Book>

    /**
     * @throws BookNotFoundException
     */
    fun find(id: Id): Book

    fun create(request: BookCreateRequest): Book

    /**
     * @throws BookNotFoundException
     */
    fun update(request: BookUpdateRequest): Book

    /**
     * @throws BookNotFoundException
     */
    fun delete(username: String, id: Id): Book
}

@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val idGenerator: IdGenerator,
) : BookService {

    private val log = logger {}

    override fun findAll(search: Search) =
        bookRepository.findAll(search)

    override fun find(id: Id) =
        bookRepository.findOrNull(id)
            ?: throw BookNotFoundException(id)

    override fun create(request: BookCreateRequest): Book {
        log.info { "create: $request" }
        val user = userRepository.findOrNull(request.username)
            ?: throw InternalException("System invariance violated! User not found: '${request.username}'")

        return newBook(request, user).also {
            bookRepository.create(it)
        }
    }

    override fun update(request: BookUpdateRequest): Book {
        log.info { "update: $request" }
        val found = bookRepository.findOrNull(request.id) ?: throw BookNotFoundException(request.id)
        // if(found.author.username != request.username) // FUTURE hardening necessary?!

        return found.updateBy(request).also {
            bookRepository.update(it)
        }
    }

    override fun delete(username: String, id: Id): Book {
        log.info { "delete: $id" }
        val book = bookRepository.findOrNull(id) ?: throw BookNotFoundException(id)
        require(book.state != BookState.Unpublished)
        // if(book.author.username != username) // FUTURE hardening necessary?!

        return book.copy(state = BookState.Unpublished).also {
            bookRepository.update(it)
        }
    }

    private fun newBook(request: BookCreateRequest, user: User) = Book(
        id = idGenerator.generate(),
        title = request.title,
        description = request.description,
        author = user,
        price = request.price,
        state = BookState.Published,
    )
}

private fun Book.updateBy(update: BookUpdateRequest) = copy(
    title = update.title,
    description = update.description,
    price = update.price,
)
