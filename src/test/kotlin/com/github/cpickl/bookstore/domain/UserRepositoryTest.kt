package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataJpaTest
abstract class UserRepositoryTest {

    private val id1 = Id(UUID1)
    private val id2 = Id(UUID2)
    private val username = "testUsername"
    private val username1 = "testUsername1"
    private val username2 = "testUsername2"
    private val pseudonym1 = "pseudonym1"
    private val pseudonym2 = "pseudonym2"

    abstract val testee: UserRepository

    @Test
    fun `When find non existing user Then return null`() {
        assertThat(testee.findByUsername("invalid")).isNull()
    }

    @Test
    fun `Given created user When find it Then return it`() {
        val user = User.any().copy(username = username)
        testee.create(user)

        val actual = testee.findByUsername(username)

        assertThat(actual).isEqualTo(user)
    }

    @Test
    fun `Given created user When find other Then return null`() {
        val user = User.any().copy(username = username1)
        testee.create(user)

        val actual = testee.findByUsername(username2)

        assertThat(actual).isNull()
    }

    @Test
    fun `Given created user When create with same ID again Then update`() {
        val user = User.any()
        val updatedUser = user.copy(authorPseudonym = pseudonym2)
        testee.create(user.copy(authorPseudonym = pseudonym1))

        assertThat {
            testee.create(updatedUser)
        }.isSuccess()

        assertThat(testee.findByUsername(user.username)).isEqualTo(updatedUser)
    }

    @Test
    fun `Given created user When create with same username again Then fail`() {
        testee.create(User.any().copy(id = id1, username = username))

        assertThat {
            testee.create(User.any().copy(id = id2, username = username))
        }.isFailure().messageContains("username")
    }

}
