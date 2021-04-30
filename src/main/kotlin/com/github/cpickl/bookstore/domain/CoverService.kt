package com.github.cpickl.bookstore.domain

import org.springframework.stereotype.Service

interface CoverService {
    /**
     * @throws BookNotFoundException
     */
    fun find(bookId: Id): CoverImage

    /**
     * @throws BookNotFoundException
     */
    fun update(bookId: Id, request: CoverUpdateRequest): Book

    /**
     * @throws BookNotFoundException
     */
    fun delete(bookId: Id): Book
}

@Service
class CoverServiceImpl(
    private val bookRepository: BookRepository,
    private val coverRepository: CoverRepository,
) : CoverService {

    override fun find(bookId: Id): CoverImage {
        val book = bookRepository.find(bookId) ?: throw BookNotFoundException(bookId)
        return coverRepository.find(book.id) ?: CoverImage.DefaultImage
    }

    override fun update(bookId: Id, request: CoverUpdateRequest): Book {
        val book = bookRepository.find(bookId) ?: throw BookNotFoundException(bookId)
        coverRepository.insertOrUpdate(book.id, CoverImage.CustomImage(request.bytes))
        return book
    }

    override fun delete(bookId: Id): Book {
        val book = bookRepository.find(bookId) ?: throw BookNotFoundException(bookId)
        coverRepository.delete(bookId)
        return book
    }
}
