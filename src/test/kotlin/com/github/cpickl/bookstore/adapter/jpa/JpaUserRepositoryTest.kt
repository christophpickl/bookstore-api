package com.github.cpickl.bookstore.adapter.jpa

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepositoryTest
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
class JpaUserRepositoryTest : UserRepositoryTest() {

    @Autowired
    private lateinit var em: TestEntityManager

    @Autowired
    private lateinit var crudRepo: JpaUserCrudRepository
    private lateinit var repo: JpaUserRepository

    private val userEntity = UserJpa.any()
    private val username1 = "username1"
    private val username2 = "username2"

    @BeforeEach
    fun `init repo`() {
        repo = JpaUserRepository(crudRepo)
    }

    override fun testee() = repo

    @Nested
    inner class FindTest {
        @Test
        fun `Given user When find by different name Then return null`() {
            em.persistAndFlush(UserJpa.any().copy(username = username1))

            val found = repo.findOrNull(username2)

            assertThat(found).isNull()
        }

        @Test
        fun `Given user When find by that name Then return it`() {
            em.persistAndFlush(userEntity)

            val found = repo.findOrNull(userEntity.username)

            assertThat(found).isEqualTo(userEntity.toUser())
        }
    }

    @Nested
    inner class CreateTest {
        @Test
        fun `When create Then persisted`() {
            repo.create(userEntity.toUser())

            assertThat(em.find(UserJpa::class.java, userEntity.id)).isEqualTo(userEntity)
        }
    }

}

private fun UserJpa.toUser() = User(
    id = Id(id),
    authorPseudonym = authorPseudonym,
    username = username,
    passwordHash = passwordHash,
)
