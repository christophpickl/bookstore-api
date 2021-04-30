package com.github.cpickl.bookstore.adapter.jpa

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import assertk.assertions.isSuccess
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.User
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
class JpaUserRepositoryTest {

    @Autowired
    private lateinit var em: TestEntityManager

    @Autowired
    private lateinit var crudRepo: JpaUserCrudRepository
    private lateinit var repo: JpaUserRepository

    private val userJpa = UserJpa.any()
    private val username = "testUsername"
    private val anyUsername = "anyUsername"
    private val username1 = "username1"
    private val username2 = "username2"
    private val pseudonym1 = "pseudonym1"
    private val pseudonym2 = "pseudonym2"

    @BeforeEach
    fun `init repo`() {
        repo = JpaUserRepository(crudRepo)
    }

    @Nested
    inner class FindTest {
        @Test
        fun `When find non existing user Then return null`() {
            val found = repo.find(anyUsername)

            assertThat(found).isNull()
        }

        @Test
        fun `Given user When find by different name Then return null`() {
            save(UserJpa.any().copy(username = username1))

            val found = repo.find(username2)

            assertThat(found).isNull()
        }

        @Test
        fun `Given user When find by that name Then return it`() {
            save(userJpa)

            val found = repo.find(userJpa.username)

            assertThat(found).isEqualTo(userJpa.toUser())
        }
    }

    @Nested
    inner class CreateTest {
        @Test
        fun `When create Then persisted`() {
            repo.create(userJpa.toUser())

            assertThat(em.find(UserJpa::class.java, userJpa.id)).isEqualTo(userJpa)
        }

        @Test
        fun `Given created user When create with same ID again Then update`() {
            save(userJpa.copy(authorPseudonym = pseudonym1))
            val updatedUser = userJpa.copy(authorPseudonym = pseudonym2)

            assertThat {
                repo.create(updatedUser.toUser())
            }.isSuccess()

            assertThat(repo.find(userJpa.username)).isEqualTo(updatedUser.toUser())
        }

        @Test
        fun `Given created user When create with same username again Then fail`() {
            repo.create(User.any().copy(id = Id.some1, username = username))

            assertThat {
                repo.create(User.any().copy(id = Id.some2, username = username))
                em.flush()
            }.isFailure()
        }
    }

    private fun save(user: UserJpa) {
        em.persistAndFlush(user)
    }
}

private fun UserJpa.toUser() = User(
    id = Id(id),
    authorPseudonym = authorPseudonym,
    username = username,
    passwordHash = passwordHash,
)
