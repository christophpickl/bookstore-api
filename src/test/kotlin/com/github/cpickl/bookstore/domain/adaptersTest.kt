package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.*
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

        testee.save(user)
        val actual = testee.findOrNull(username)

        assertThat(actual).isEqualTo(user)
    }

    abstract fun testee(): UserRepository
}


abstract class BookRepositoryTest {

    @Test
    fun `find empty`() {
        assertThat(testee().findAll()).isEmpty()
    }


    @Test
    fun `save and find finds`() {
        val testee = testee()
        val book = Book.any()

        testee.save(book)
        val found = testee.findAll()

        assertThat(found).containsExactly(book)
    }



    abstract fun testee(): BookRepository
}