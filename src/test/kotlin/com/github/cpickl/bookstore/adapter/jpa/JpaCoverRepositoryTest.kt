package com.github.cpickl.bookstore.adapter.jpa

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isSuccess
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.domain.CoverImage
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.any
import com.github.cpickl.bookstore.domain.some1
import com.github.cpickl.bookstore.domain.some2
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataJpaTest
class JpaCoverRepositoryTest {

    @Autowired
    private lateinit var em: TestEntityManager

    @Autowired
    private lateinit var coverCrudRepo: JpaCoverCrudRepository

    @Autowired
    private lateinit var bookCrudRepo: JpaBookCrudRepository
    private lateinit var repo: JpaCoverRepository

    private val id = Id.any()
    private val image = CoverImage.CustomImage.any()
    private val image1 = CoverImage.CustomImage(byteArrayOf(0, 1))
    private val image2 = CoverImage.CustomImage(byteArrayOf(1, 1))
    private val anyBytes = byteArrayOf(0)

    @BeforeEach
    fun `init repo`() {
        repo = JpaCoverRepository(coverCrudRepo, bookCrudRepo)
    }

    @Nested
    inner class FindTest {
        @Test
        fun `When find unknown Then return null`() {
            val found = repo.findById(Id.any())

            assertThat(found).isNull()
        }

        @Test
        fun `Given book and cover When find it Then return it`() {
            val book = saveBookAndUser(bookId = id)
            save(CoverJpa(book, image.bytes))

            val found = repo.findById(id)

            assertThat(found).isEqualTo(image)
        }

        @Test
        fun `Given book and cover When find by different ID Then return null`() {
            val book = saveBookAndUser(bookId = Id.some1)
            save(CoverJpa(book, anyBytes))

            val found = repo.findById(Id.some2)

            assertThat(found).isNull()
        }
    }

    @Nested
    inner class InsertUpdateTest {
        @Test
        fun `When insertOrUpdate without book Then fail`() {
            assertThat {
                repo.insertOrUpdate(Id.any(), CoverImage.CustomImage.any())
            }.isFailure()
        }

        @Test
        fun `Given book When insertOrUpdate cover Then persisted`() {
            val book = saveBookAndUser(bookId = id)

            repo.insertOrUpdate(id, image)

            assertThat(find(id)).isEqualTo(CoverJpa(book, image.bytes))
        }

        @Test
        fun `Given book and cover When insertOrUpdate it Then return updated`() {
            val book = saveBookAndUser(bookId = id)
            save(CoverJpa(book, image1.bytes))

            repo.insertOrUpdate(id, image2)

            assertThat(find(id)).isEqualTo(CoverJpa(book, image2.bytes))
        }
    }

    @Nested
    inner class DeleteTest {
        @Test
        fun `Given book and cover When delete cover Then cover is gone`() {
            val book = saveBookAndUser(bookId = id)
            save(CoverJpa(book, anyBytes))

            repo.delete(id)

            assertThat(find(id)).isNull()
        }

        @Test
        fun `Given book and cover When delete cover Then book is still there`() {
            val book = saveBookAndUser(bookId = id)
            save(CoverJpa(book, anyBytes))

            repo.delete(id)

            assertThat(em.find(BookJpa::class.java, +id)).isNotNull()
        }

        @Test
        fun `When delete non existing Then do nothing`() {
            assertThat {
                repo.delete(Id.any())
            }.isSuccess()
        }
    }

    private fun find(id: Id): CoverJpa? {
        return em.find(CoverJpa::class.java, +id)
    }

    private fun save(cover: CoverJpa) {
        em.persistAndFlush(cover)
    }

    private fun saveBookAndUser(bookId: Id = Id.any()): BookJpa {
        val user = em.persistAndFlush(UserJpa.any())
        return em.persistAndFlush(BookJpa.any().copy(id = +bookId, author = user))
    }
}
