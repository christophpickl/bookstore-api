package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

abstract class BookRepositoryTest {

    private lateinit var testee: BookRepository
    private val book = Book.any().copy(state = BookState.Published)

    @BeforeEach
    fun `init testee`() {
        testee = testee()
    }

    @Nested
    inner class FindAllTest {

        @Test
        fun `When find all Then return empty`() {
            assertThat(testee.findAll()).isEmpty()
        }

        @Test
        fun `Given created book When find all Then finds`() {
            testee.create(book)

            val found = testee.findAll()

            assertThat(found).containsExactly(book)
        }

        @Test
        fun `Given two books When find all Then return sorted`() {
            testee.create(Book.any().copy(title = "b", state = BookState.Published))
            testee.create(Book.any().copy(title = "a", state = BookState.Published))

            val found = testee.findAll()

            assertThat(found.map { it.title }).containsExactly("a", "b")
        }

        @Test
        fun `Given book When find all search off Then return single book`() {
            testee.create(book)

            val found = testee.findAll(Search.Off)

            assertThat(found).containsExactly(book)
        }

        @Test
        fun `Given book When search book Then return that book`() {
            val book = book.copy(title = "xax")
            testee.create(book)

            val found = testee.findAll(Search.On("a"))

            assertThat(found).containsExactly(book)
        }

        @Test
        fun `Given book When search book different cased Then return that book`() {
            val book = book.copy(title = "A")
            testee.create(book)

            val found = testee.findAll(Search.On("a"))

            assertThat(found).containsExactly(book)
        }

        @Test
        fun `Given book When search term different cased Then return that book`() {
            val book = book.copy(title = "a")
            testee.create(book)

            val found = testee.findAll(Search.On("A"))

            assertThat(found).containsExactly(book)
        }

        @Test
        fun `Given book When search for unknown Then return empty`() {
            testee.create(book.copy(title = "a"))

            val found = testee.findAll(Search.On("x"))

            assertThat(found).isEmpty()
        }

        @Test
        fun `Given unpublished book When find all Then return empty`() {
            testee.create(book.copy(state = BookState.Unpublished))

            val found = testee.findAll()

            assertThat(found).isEmpty()
        }
    }

    @Nested
    inner class FindSingleTest {

        @Test
        fun `When find single Then return null`() {
            assertThat(testee.findOrNull(Id.any())).isNull()
        }

        @Test
        fun `Given created book When find single Then finds`() {
            testee.create(book)

            val found = testee.findOrNull(book.id)

            assertThat(found).isEqualTo(book)
        }

        @Test
        fun `Given unpublished book When find single Then return null`() {
            testee.create(book.copy(state = BookState.Unpublished))

            val found = testee.findOrNull(book.id)

            assertThat(found).isNull()
        }

    }

    @Nested
    inner class CreateTest {
        @Test
        fun `Given created book When create with same ID Then fail`() {
            testee.create(book)

            assertThat {
                testee.create(book)
            }.isFailure()
        }
    }

    @Nested
    inner class UpdateTest {
        @Test
        fun `When update non existing Then fail`() {
            assertThat {
                testee.update(book)
            }.isFailure()
        }

        @Test
        fun `Given inserted book When update Then get updated back on find`() {
            val updated = book.copy(title = "title2")
            testee.create(book)

            testee.update(updated)

            assertThat(testee.findOrNull(book.id)).isEqualTo(updated)
        }
    }

    abstract fun testee(): BookRepository
}