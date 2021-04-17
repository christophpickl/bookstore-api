package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test

abstract class UserRepositoryTest {

    private val username = "testUsername"

    @Test
    fun `find is null`() {
        assertThat(testee().findOrNull("invalid")).isNull()
    }

    @Test
    fun `save and find succeeds`() {
        val testee = testee()
        val user = User.any().copy(username = username)

        testee.create(user)
        val actual = testee.findOrNull(username)

        assertThat(actual).isEqualTo(user)
    }

    abstract fun testee(): UserRepository
}
