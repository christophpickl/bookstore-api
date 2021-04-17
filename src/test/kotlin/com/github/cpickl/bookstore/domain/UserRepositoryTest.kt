package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class UserRepositoryTest {

    private lateinit var testee: UserRepository
    private val username = "testUsername"

    @BeforeEach
    fun `init testee`() {
        testee = testee()
    }

    @Test
    fun `When find non existing user Then return null`() {
        assertThat(testee.findOrNull("invalid")).isNull()
    }

    @Test
    fun `Given created user When find it Then return it`() {
        val user = User.any().copy(username = username)
        testee.create(user)

        val actual = testee.findOrNull(username)

        assertThat(actual).isEqualTo(user)
    }

    abstract fun testee(): UserRepository
}
