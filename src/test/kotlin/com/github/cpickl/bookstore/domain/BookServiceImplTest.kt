package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.BeforeEach
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

    @Test
    fun `find all delegates to repo`() {
        whenever(bookRepository.findAll()).thenReturn(books)

        val found = service.findAll()

        assertThat(found).isEqualTo(books)
        verify(bookRepository, times(1)).findAll()
        verifyNoMoreInteractions(bookRepository)
    }

    @Test
    fun `find single delegates to repo`() {
        whenever(bookRepository.findOrNull(book.id)).thenReturn(book)

        val found = service.findOrNull(book.id)

        assertThat(found).isEqualTo(book)
        verify(bookRepository, times(1)).findOrNull(book.id)
        verifyNoMoreInteractions(bookRepository)
    }

    @Test
    fun `save delegates to repo`() {
        val user = User.any().copy(username = username)
        whenever(userRepository.findOrNull(username)).thenReturn(user)
        val request = BookCreateRequest.any().copy(username = username)

        val created = service.create(request)

        val expected = Book(
            id = created.id,
            title = request.title,
            description = request.description,
            author = user,
            cover = Image.empty().copy(id = created.cover.id),
            price = Amount.euroCent(request.euroCent)
        )
        assertThat(created).isEqualTo(expected)
        verify(bookRepository, times(1)).create(expected)
    }

    @Test
    fun `update delegates to repo`() {
        val request = BookUpdateRequest(username, book.id,"title2")
        val updated = book.copy(title = request.title)
        whenever(bookRepository.findOrNull(book.id)).thenReturn(book)

        val actual = service.update(request)

        assertThat(actual).isEqualTo(updated)
        verify(bookRepository, times(1)).update(updated)
    }
}
