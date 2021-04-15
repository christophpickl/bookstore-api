package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class BookServiceImplTest {

    private lateinit var service: BookServiceImpl
    private lateinit var bookRepository: BookRepository
    private lateinit var userRepository: UserRepository

    private val username = "myUsername"

    @BeforeEach
    fun `init mocks`() {
        bookRepository = mock()
        userRepository = mock()
        service = BookServiceImpl(bookRepository, userRepository)
    }

    @Test
    fun `find delegates to repo`() {
        val books = listOf(Book.any())
        whenever(bookRepository.findAll()).thenReturn(books)

        val found = service.getBooks()

        assertThat(found).isEqualTo(books)
        verify(bookRepository, times(1)).findAll()
        verifyNoMoreInteractions(bookRepository)
    }

    @Test
    fun `save delegates to repo`() {
        val user = User.any().copy(username = username)
        whenever(userRepository.findOrNull(username)).thenReturn(user)
        val request = BookCreateRequest.any().copy(username = username)
        val created = service.createBook(request)

        val expected = Book(
            id = created.id,
            title = request.title,
            description = request.description,
            author = user,
            cover = Image.empty().copy(id = created.cover.id),
            price = Amount.euroCent(request.euroCent)
        )
        assertThat(created).isEqualTo(expected)
        verify(bookRepository, times(1)).save(expected)
        verifyNoMoreInteractions(bookRepository)
    }
}
