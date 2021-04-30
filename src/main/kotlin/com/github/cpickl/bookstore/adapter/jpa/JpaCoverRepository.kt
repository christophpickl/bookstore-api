package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.domain.CoverImage
import com.github.cpickl.bookstore.domain.CoverRepository
import com.github.cpickl.bookstore.domain.Id
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class JpaCoverRepository(
    private val jpaRepo: JpaCoverCrudRepository,
) : CoverRepository {

    override fun find(bookId: Id): CoverImage.CustomImage? =
        jpaRepo.findById(bookId.toString()).unwrap {
            CoverImage.CustomImage(bytes = it.bytes)
        }

    override fun insertOrUpdate(bookId: Id, image: CoverImage.CustomImage) {
        jpaRepo.save(CoverJpa(bookId.toString(), image.bytes))
    }

    override fun delete(bookId: Id): CoverImage.CustomImage? {
        val cover = find(bookId) ?: return null
        jpaRepo.deleteById(bookId.toString())
        return CoverImage.CustomImage(cover.bytes)
    }
}

fun <IN, OUT> Optional<IN>.unwrap(transform: (IN) -> OUT): OUT? =
    if (isEmpty) {
        null
    } else {
        transform(get())
    }

interface JpaCoverCrudRepository : CrudRepository<CoverJpa, String>
