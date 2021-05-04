package com.github.cpickl.bookstore.adapter.jpa

import assertk.assertThat
import assertk.assertions.isEqualTo
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
    private lateinit var crudRepo: JpaCoverCrudRepository
    private lateinit var repo: JpaCoverRepository

    private val id = Id.any()
    private val image = CoverImage.CustomImage.any()
    private val image1 = CoverImage.CustomImage(byteArrayOf(0, 1))
    private val image2 = CoverImage.CustomImage(byteArrayOf(1, 1))
    private val anyBytes = byteArrayOf(0)

    @BeforeEach
    fun `init repo`() {
        repo = JpaCoverRepository(crudRepo)
    }

    @Nested
    inner class FindTest {
        @Test
        fun `When find unknown Then return null`() {
            val found = repo.findById(Id.any())

            assertThat(found).isNull()
        }

        @Test
        fun `Given cover When find it Then return it`() {
            save(CoverJpa(+id, image.bytes))

            val found = repo.findById(id)

            assertThat(found).isEqualTo(image)
        }

        @Test
        fun `Given cover When find by different ID Then return null`() {
            save(CoverJpa(+Id.some1, anyBytes))

            val found = repo.findById(Id.some2)

            assertThat(found).isNull()
        }
    }

    @Nested
    inner class InsertUpdateTest {
        @Test
        fun `When insertOrUpdate cover Then persisted`() {
            repo.insertOrUpdate(id, image)

            assertThat(find(id)).isEqualTo(CoverJpa(+id, image.bytes))
        }

        @Test
        fun `Given cover When insertOrUpdate it Then return updated`() {
            save(CoverJpa(+id, image1.bytes))

            repo.insertOrUpdate(id, image2)

            assertThat(find(id)).isEqualTo(CoverJpa(+id, image2.bytes))
        }
    }

    @Nested
    inner class DeleteTest {
        @Test
        fun `Given cover When delete again Then return null for find`() {
            save(CoverJpa(+id, anyBytes))

            repo.delete(id)

            assertThat(find(id)).isNull()
        }

        @Test
        fun `When delete non existing Then do nothing`() {
            assertThat {
                repo.delete(id)
            }.isSuccess()
        }
    }

    private fun find(id: Id): CoverJpa? {
        return em.find(CoverJpa::class.java, +id)
    }

    private fun save(cover: CoverJpa) {
        em.persistAndFlush(cover)
    }
}
