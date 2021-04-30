package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import com.github.cpickl.bookstore.boundary.BookUpdateDto
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.boundary.toBookUpdateRequest
import com.github.cpickl.bookstore.throws
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class BookServiceImplTest {

    private lateinit var service: BookServiceImpl
    private lateinit var bookRepository: BookRepository
    private lateinit var userRepository: UserRepository
    private lateinit var idGenerator: IdGenerator
    private val book = Book.any()
    private val books = listOf(book)
    private val username = "myUsername"

    @BeforeEach
    fun `init mocks`() {
        bookRepository = mock()
        userRepository = mock()
        idGenerator = RandomIdGenerator
        service = BookServiceImpl(bookRepository, userRepository, idGenerator)
    }

    @Nested
    inner class FindAllTest {
        @Test
        fun `find all delegates to repo`() {
            whenever(bookRepository.findAll()).thenReturn(books)

            val found = service.findAll()

            assertThat(found).isEqualTo(books)
            verify(bookRepository, times(1)).findAll()
            verifyNoMoreInteractions(bookRepository)
        }

    }

    @Nested
    inner class FindSingleTest {
        @Test
        fun `find single delegates to repo`() {
            whenever(bookRepository.find(book.id)).thenReturn(book)

            val found = service.find(book.id)

            assertThat(found).isEqualTo(book)
            verify(bookRepository, times(1)).find(book.id)
            verifyNoMoreInteractions(bookRepository)
        }
    }

    @Nested
    inner class CreateTest {
        @Test
        fun `Given user When create Then return new book and delegate to repo`() {
            val user = User.any().copy(username = username)
            whenever(userRepository.find(username)).thenReturn(user)
            val request = BookCreateRequest.any().copy(username = username)

            val created = service.create(request)

            val expected = Book(
                id = created.id,
                title = request.title,
                description = request.description,
                author = user,
                price = request.price,
                state = BookState.Published,
            )
            assertThat(created).isEqualTo(expected)
            verify(bookRepository, times(1)).create(expected)
        }

        @Test
        fun `Given user not exists When create book Then throw`() {
            whenever(userRepository.find(username)).thenReturn(null)
            val request = BookCreateRequest.any().copy(username = username)

            assertThat {
                service.create(request)

            }.throws<InternalException>(messageContains = username)
        }
    }

    @Nested
    inner class UpdateTest {
        @Test
        fun `update delegates to repo`() {
            val request = BookUpdateDto.any().toBookUpdateRequest(username, book.id)
            whenever(bookRepository.find(book.id)).thenReturn(book)

            val actual = service.update(request)

            val updated = book.copy(
                title = request.title,
                description = request.description,
                price = request.price,
            )
            assertThat(actual).isEqualTo(updated)
            verify(bookRepository, times(1)).update(updated)
        }
    }

    @Nested
    inner class DeleteTest {
        @Test
        fun `Given published book When delete it Then update to unpublished`() {
            val book = book.copy(state = BookState.Published)
            whenever(bookRepository.find(book.id)).thenReturn(book)

            val actual = service.delete(username, book.id)

            val deleted = book.copy(state = BookState.Unpublished)
            assertThat(actual).isEqualTo(deleted)
            verify(bookRepository).update(deleted)
        }

        @Test
        fun `Given unpublished book When delete it Then fail`() {
            val book = book.copy(state = BookState.Unpublished)
            whenever(bookRepository.find(book.id)).thenReturn(book)

            assertThat {
                service.delete(username, book.id)

            }.isFailure()
        }

        @Test
        fun `Given book not existing When delete it Then throw not found`() {
            whenever(bookRepository.find(book.id)).thenReturn(null)

            assertThat {
                service.delete(username, book.id)

            }.throws<BookNotFoundException>(messageContains = +book.id)
        }
    }
}
