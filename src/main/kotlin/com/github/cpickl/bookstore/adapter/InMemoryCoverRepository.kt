package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.CoverImage
import com.github.cpickl.bookstore.domain.CoverRepository
import com.github.cpickl.bookstore.domain.Id
import org.springframework.stereotype.Repository

@Repository
class InMemoryCoverRepository : CoverRepository {

    private val imagesById = mutableMapOf<Id, CoverImage.CustomImage>()

    override fun findOrNull(bookId: Id) = imagesById[bookId]
}
