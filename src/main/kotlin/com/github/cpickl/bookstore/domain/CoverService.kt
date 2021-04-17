package com.github.cpickl.bookstore.domain

import org.springframework.stereotype.Service

interface CoverService {
    fun find(bookId: Id): CoverImage?
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
}
