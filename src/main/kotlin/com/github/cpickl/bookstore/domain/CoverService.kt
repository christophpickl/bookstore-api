package com.github.cpickl.bookstore.domain

import org.springframework.stereotype.Service

interface CoverService {
    fun find(bookId: Id): CoverImage?
    fun update(bookId: Id, request: CoverUpdateRequest): Book?
    fun delete(bookId: Id): Book?
}

@Service
class CoverServiceImpl(
    private val bookRepository: BookRepository,
    private val coverRepository: CoverRepository,
) : CoverService {

    override fun find(bookId: Id): CoverImage? {
        val book = bookRepository.findOrNull(bookId) ?: return null
        return coverRepository.findOrNull(book.id) ?: CoverImage.DefaultImage
    }

    override fun update(bookId: Id, request: CoverUpdateRequest): Book? {
        val book = bookRepository.findOrNull(bookId) ?: return null
        coverRepository.update(book.id, CoverImage.CustomImage(request.bytes))
        return book
    }

    override fun delete(bookId: Id): Book? {
        val book = bookRepository.findOrNull(bookId) ?: return null
        coverRepository.delete(bookId)
        return book
    }
}
