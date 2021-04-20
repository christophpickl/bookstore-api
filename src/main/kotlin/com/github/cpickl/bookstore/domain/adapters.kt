package com.github.cpickl.bookstore.domain

interface BookRepository {
    fun findAll(search: Search = Search.Off): List<Book>
    fun findOrNull(id: Id): Book?
    // FUTURE should check for unique ID
    fun create(book: Book)
    fun update(book: Book)
}

interface UserRepository {
    fun findOrNull(username: String): User?
    // FUTURE should check for unique ID and username
    fun create(user: User)
}

interface CoverRepository {
    fun findOrNull(bookId: Id): CoverImage.CustomImage?
    fun update(bookId: Id, image: CoverImage.CustomImage)
    fun delete(bookId: Id): CoverImage.CustomImage?
}
