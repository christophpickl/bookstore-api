package com.github.cpickl.bookstore.domain

import org.springframework.stereotype.Service

interface BookService {
    fun findAll(search: Search = Search.Off): List<Book>
    fun findOrNull(id: Id): Book?
    fun create(request: BookCreateRequest): Book
    fun update(request: BookUpdateRequest): Book?
}

sealed class Search {
    object Off : Search()
    // FUTURE support multiple terms (and wildcards)
    class On(term: String) : Search() {
        init {
            require(term.trim().isNotEmpty())
        }
        val term = term.toLowerCase() // FUTURE with kotlin 1.5 use lowercase()

        override fun equals(other: Any?): Boolean {
            if(other !is On) return false
            return this.term == other.term
        }

        override fun hashCode() = term.hashCode()
    }
}

data class BookCreateRequest(
    val username: String,
    val title: String,
    val description: String,
    val euroCent: Int,
) {
    companion object
}

data class BookUpdateRequest(
    val username: String,
    val id: Id,
    val title: String,
)

@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val idGenerator: IdGenerator,
) : BookService {

    override fun findAll(search: Search) = bookRepository.findAll(search)

    override fun findOrNull(id: Id) = bookRepository.findOrNull(id)

    override fun create(request: BookCreateRequest): Book {
        val user = userRepository.findOrNull(request.username)
            ?: throw IllegalArgumentException("User not found: '${request.username}'")
        val book = Book(
            id = idGenerator.generate(),
            title = request.title,
            description = request.description,
            author = user,
            cover = Image.empty(), // FIXME implement images
            price = Amount.euroCent(request.euroCent),
        )
        bookRepository.create(book)
        return book
    }

    override fun update(request: BookUpdateRequest): Book? {
        val found = bookRepository.findOrNull(request.id) ?: return null
        // FUTURE hardening necessary?!
        // if(found.author.username != request.username) throw Exception("Not your book!")

        val updated = found.updateBy(request)
        bookRepository.update(updated)

        return updated
    }
}

private fun Book.updateBy(update: BookUpdateRequest) = copy(
    title = update.title
)
