package com.github.cpickl.bookstore.domain

interface BookRepository {
    fun findAll(search: Search = Search.Off): List<Book>
    fun find(id: Id): Book?
    fun create(book: Book)
    fun update(book: Book)
}

interface UserRepository {
    fun find(username: String): User?
    fun create(user: User)
}

interface CoverRepository {
    fun find(bookId: Id): CoverImage.CustomImage?
    fun insertOrUpdate(bookId: Id, image: CoverImage.CustomImage)
    fun delete(bookId: Id): CoverImage.CustomImage?
}
