package com.github.cpickl.bookstore.domain

import org.springframework.stereotype.Service
import java.util.*

interface BookService {
    fun getBooks(): List<Book>
    fun createBook(request: BookCreateRequest): Book
}

data class BookCreateRequest(
    val username: String,
    val title: String,
    val description: String,
    val euroCent: Int,
) {
    companion object
}


@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
) : BookService {

    override fun getBooks() = bookRepository.findAll()

    override fun createBook(request: BookCreateRequest): Book {
        val user = userRepository.findOrNull(request.username)  ?:
        throw IllegalArgumentException("User not found: '${request.username}'")
        val book = Book(
            id = UUID.randomUUID(),
            title = request.title,
            description = request.description,
            author = user,
            cover = Image.empty(), // TODO implement images
            price = Amount.euroCent(request.euroCent),
        )
        bookRepository.save(book)
        return book
    }

}
