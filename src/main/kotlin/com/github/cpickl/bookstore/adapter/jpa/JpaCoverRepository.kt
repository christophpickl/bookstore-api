package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.common.unwrap
import com.github.cpickl.bookstore.domain.BookNotFoundException
import com.github.cpickl.bookstore.domain.CoverImage
import com.github.cpickl.bookstore.domain.CoverRepository
import com.github.cpickl.bookstore.domain.Id
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JpaCoverRepository(
    private val coverRepo: JpaCoverCrudRepository,
    private val bookRepo: JpaBookCrudRepository,
) : CoverRepository {

    // TODO add paramter to filter UNPUBLISHED (published logic should be in service-domain layer, not here)

    @Transactional(readOnly = true)
    override fun findById(bookId: Id): CoverImage.CustomImage? =
        coverRepo.findById(bookId.toString()).unwrap {
            CoverImage.CustomImage(bytes = it.bytes)
        }

    @Transactional
    override fun insertOrUpdate(bookId: Id, image: CoverImage.CustomImage) {
        val book = bookRepo.findById(+bookId).orElseThrow { BookNotFoundException(bookId) }
        val cover = CoverJpa(bookId.toString(), book, image.bytes)
        coverRepo.save(cover)
    }

    @Transactional
    override fun delete(bookId: Id): CoverImage.CustomImage? {
        val cover = findById(bookId) ?: return null
        coverRepo.deleteById(bookId.toString())
        return CoverImage.CustomImage(cover.bytes)
    }
}

interface JpaCoverCrudRepository : CrudRepository<CoverJpa, String>
