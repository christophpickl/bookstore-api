package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test

abstract class UserRepositoryTest {

    private val username = "testUsername"
    private val username1 = "testUsername1"
    private val username2 = "testUsername2"

    @Test
    fun `When find non existing user Then return null`() {
        assertThat(testee().findOrNull("invalid")).isNull()
    }

    @Test
    fun `Given created user When find it Then return it`() {
        val user = User.any().copy(username = username)
        testee().create(user)

        val actual = testee().findOrNull(username)

        assertThat(actual).isEqualTo(user)
    }

    @Test
    fun `Given created user When find other Then return null`() {
        val user = User.any().copy(username = username1)
        testee().create(user)

        val actual = testee().findOrNull(username2)

        assertThat(actual).isNull()
    }

    abstract fun testee(): UserRepository
}
