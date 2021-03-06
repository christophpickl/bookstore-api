package com.github.cpickl.bookstore.adapter.jpa

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isSuccess
import assertk.assertions.isTrue
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Role
import com.github.cpickl.bookstore.domain.UUID1
import com.github.cpickl.bookstore.domain.UUID2
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
    private lateinit var userCrudRepo: JpaUserCrudRepository

    private lateinit var repo: JpaUserRepository

    private val userJpa = UserJpa.any()
    private val username = "testUsername"
    private val id = Id.any()
    private val id1 = Id(UUID1)
    private val id2 = Id(UUID2)
    private val anyId = id
    private val anyUsername = "anyUsername"
    private val username1 = "username1"
    private val username2 = "username2"
    private val pseudonym1 = "pseudonym1"
    private val pseudonym2 = "pseudonym2"

    @BeforeEach
    fun `init repo`() {
        repo = JpaUserRepository(userCrudRepo)
    }

    @Nested
    inner class FindByIdTest {
        @Test
        fun `When find non existing user Then return null`() {
            val found = repo.findById(anyId)

            assertThat(found).isNull()
        }

        @Test
        fun `Given user When find by different name Then return null`() {
            save(UserJpa.any().copy(id = +id1))

            val found = repo.findById(id2)

            assertThat(found).isNull()
        }

        @Test
        fun `Given user When find by that name Then return it`() {
            save(userJpa)

            val found = repo.findById(Id(userJpa.id))

            assertThat(found).isEqualTo(userJpa.toUser())
        }
    }

    @Nested
    inner class FindByUsernameTest {
        @Test
        fun `When find non existing user Then return null`() {
            val found = repo.findByUsername(anyUsername)

            assertThat(found).isNull()
        }

        @Test
        fun `Given user When find by different name Then return null`() {
            save(UserJpa.any().copy(username = username1))

            val found = repo.findByUsername(username2)

            assertThat(found).isNull()
        }

        @Test
        fun `Given user When find by that name Then return it`() {
            save(userJpa)

            val found = repo.findByUsername(userJpa.username)

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

            assertThat(repo.findByUsername(userJpa.username)).isEqualTo(updatedUser.toUser())
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

    @Nested
    inner class IsEmptyTest {

        @Test
        fun `When is empty Then return true`() {
            assertThat(repo.isEmpty()).isTrue()
        }

        @Test
        fun `Given a user When is empty Then return false`() {
            save(UserJpa.any())

            assertThat(repo.isEmpty()).isFalse()
        }
    }

    private fun save(user: UserJpa) {
        em.persistAndFlush(user)
    }
}
