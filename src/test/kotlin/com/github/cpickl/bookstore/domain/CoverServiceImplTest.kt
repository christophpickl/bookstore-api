package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CoverServiceImplTest {

    private lateinit var service: CoverServiceImpl
    private lateinit var bookRepository: BookRepository
    private lateinit var coverRepository: CoverRepository
    private val book = Book.any()
    private val bookId = book.id
    private val customCover = CoverImage.CustomImage(byteArrayOf(0, 1))

    @BeforeEach
    fun `init mocks`() {
        bookRepository = mock()
        coverRepository = mock()
        service = CoverServiceImpl(bookRepository, coverRepository)
    }

    @Test
    fun `Given book repo cant find When find cover Then return null`() {
        whenever(bookRepository.findOrNull(bookId)).thenReturn(null)

        val found = service.find(bookId)

        assertThat(found).isNull()
    }

    @Test
    fun `Given book repo has but cover repo has not When find cover Then return default cover`() {
        whenever(bookRepository.findOrNull(bookId)).thenReturn(book)
        whenever(coverRepository.findOrNull(bookId)).thenReturn(null)

        val found = service.find(bookId)

        assertThat(found).isSameAs(CoverImage.DefaultImage)
    }

    @Test
    fun `Given book repo and cover repo has When find cover Then return cover`() {
        whenever(bookRepository.findOrNull(bookId)).thenReturn(book)
        whenever(coverRepository.findOrNull(bookId)).thenReturn(customCover)

        val found = service.find(bookId)

        assertThat(found).isSameAs(customCover)
    }
}
