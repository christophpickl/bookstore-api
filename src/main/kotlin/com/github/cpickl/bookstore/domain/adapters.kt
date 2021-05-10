package com.github.cpickl.bookstore.domain

interface UserRepository {
    fun findById(id: Id): User?
    fun findByUsername(username: String): User?
    fun create(user: User)
    fun isEmpty(): Boolean
}

interface BookRepository {
    fun findAll(search: Search = Search.Off): List<Book>
    fun findById(id: Id): Book?
    fun create(book: Book)
    fun update(book: Book)
}

interface CoverRepository {
    fun findById(bookId: Id): CoverImage.CustomImage?
    fun insertOrUpdate(bookId: Id, image: CoverImage.CustomImage)
    fun delete(bookId: Id): CoverImage.CustomImage?
}
