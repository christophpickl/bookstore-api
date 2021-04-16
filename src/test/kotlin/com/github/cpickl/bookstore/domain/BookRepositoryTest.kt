package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class BookRepositoryTest {

    private lateinit var testee: BookRepository

    @BeforeEach
    fun `init testee`() {
        testee = testee()
    }
    @Test
    fun `When find all Then return empty`() {
        assertThat(testee.findAll()).isEmpty()
    }

    @Test
    fun `When find single Then return null`() {
        assertThat(testee.findOrNull(Id.any())).isNull()
    }

    @Test
    fun `Given created book When find all Then finds`() {
        val book = Book.any()
        testee.create(book)

        val found = testee.findAll()

        assertThat(found).containsExactly(book)
    }

    @Test
    fun `Given created book When find single Then finds`() {
        val book = Book.any()
        testee.create(book)

        val found = testee.findOrNull(book.id)

        assertThat(found).isEqualTo(book)
    }

    @Test
    fun `Given created book When create with same ID Then fail`() {
        val book = Book.any()
        testee.create(book)

        assertThat {
            testee.create(book)
        }.isFailure()
    }

    @Test
    fun `When update non existing Then fail`() {
        assertThat {
            testee.update(Book.any())
        }.isFailure()
    }

    @Test
    fun `Given inserted book When update Then get updated back on find`() {
        val book = Book.any()
        val updated = book.copy(title = "title2")
        testee.create(book)

        testee.update(updated)

        assertThat(testee.findOrNull(book.id)).isEqualTo(updated)
    }

    abstract fun testee(): BookRepository
}