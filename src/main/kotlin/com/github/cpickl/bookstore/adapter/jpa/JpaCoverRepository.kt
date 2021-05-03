package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.domain.CoverImage
import com.github.cpickl.bookstore.domain.CoverRepository
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.unwrap
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
class JpaCoverRepository(
    private val repo: JpaCoverCrudRepository,
) : CoverRepository {

    override fun find(bookId: Id): CoverImage.CustomImage? =
        repo.findById(bookId.toString()).unwrap {
            CoverImage.CustomImage(bytes = it.bytes)
        }

    override fun insertOrUpdate(bookId: Id, image: CoverImage.CustomImage) {
        repo.save(CoverJpa(bookId.toString(), image.bytes))
    }

    override fun delete(bookId: Id): CoverImage.CustomImage? {
        val cover = find(bookId) ?: return null
        repo.deleteById(bookId.toString())
        return CoverImage.CustomImage(cover.bytes)
    }
}


interface JpaCoverCrudRepository : CrudRepository<CoverJpa, String>
