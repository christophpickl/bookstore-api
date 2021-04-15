package com.github.cpickl.bookstore.domain

interface BookRepository {
    fun findAll(): List<Book>
    fun save(book: Book)
}

interface UserRepository {
    fun findOrNull(username: String): User?
    fun save(user: User)
}
