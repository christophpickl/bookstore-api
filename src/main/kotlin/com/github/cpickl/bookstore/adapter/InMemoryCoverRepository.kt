package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.CoverImage
import com.github.cpickl.bookstore.domain.CoverRepository
import com.github.cpickl.bookstore.domain.Id
import mu.KotlinLogging.logger
import org.springframework.stereotype.Repository

@Repository
class InMemoryCoverRepository : CoverRepository {

    private val log = logger {}
    private val imagesById = mutableMapOf<Id, CoverImage.CustomImage>()

    override fun findOrNull(bookId: Id) = imagesById[bookId]

    override fun update(bookId: Id, image: CoverImage.CustomImage) {
        log.debug { "update: $bookId" }
        imagesById[bookId] = image
    }

    override fun delete(bookId: Id): CoverImage.CustomImage? {
        log.debug { "delete: $bookId" }
        return imagesById.remove(bookId)
    }

    fun clear() {
        log.info { "clear covers ... for TEST only!" }
        imagesById.clear()
    }
}
