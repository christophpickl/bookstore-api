package com.github.cpickl.bookstore.domain

interface BookRepository {
    fun findAll(): List<Book>
    fun findOrNull(id: Id): Book?
    fun create(book: Book)
    fun update(book: Book)
}

interface UserRepository {
    fun findOrNull(username: String): User?
    // FUTURE should check for unique ID and username
    fun create(user: User)
}
