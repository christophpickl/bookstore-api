package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isSameAs
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.throws
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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

    @Nested
    inner class FindTest {

        @Test
        fun `Given book repo cant find When find cover Then return null`() {
            whenever(bookRepository.find(bookId)).thenReturn(null)

            assertThat {
                service.find(bookId)

            }.throws<BookNotFoundException>(messageContains = +bookId)
        }

        @Test
        fun `Given book repo has but cover repo has not When find cover Then return default cover`() {
            whenever(bookRepository.find(bookId)).thenReturn(book)
            whenever(coverRepository.find(bookId)).thenReturn(null)

            val found = service.find(bookId)

            assertThat(found).isSameAs(CoverImage.DefaultImage)
        }

        @Test
        fun `Given book repo and cover repo has When find cover Then return cover`() {
            whenever(bookRepository.find(bookId)).thenReturn(book)
            whenever(coverRepository.find(bookId)).thenReturn(customCover)

            val found = service.find(bookId)

            assertThat(found).isSameAs(customCover)
        }
    }

    @Nested
    inner class UpdateTest {
        @Test
        fun `Given not book existing When update cover Then return null`() {
            whenever(bookRepository.find(bookId)).thenReturn(null)

            assertThat {
                service.update(bookId, CoverUpdateRequest.any())

            }.throws<BookNotFoundException>(messageContains = +bookId)
        }

        @Test
        fun `Given existing book When update cover Then return that book`() {
            whenever(bookRepository.find(bookId)).thenReturn(book)

            val updated = service.update(bookId, CoverUpdateRequest.any())

            assertThat(updated).isSameAs(book)
        }

        @Test
        fun `Given existing book When update cover Then delegated to repository`() {
            whenever(bookRepository.find(bookId)).thenReturn(book)
            val request = CoverUpdateRequest.any()

            service.update(bookId, request)

            verify(coverRepository).insertOrUpdate(bookId, CoverImage.CustomImage(request.bytes))
        }
    }

    @Nested
    inner class DeleteTest {
        @Test
        fun `Given not book existing When delete cover Then return null`() {
            whenever(bookRepository.find(bookId)).thenReturn(null)

            assertThat {
                service.delete(bookId)

            }.throws<BookNotFoundException>(messageContains = +bookId)
        }

        @Test
        fun `Given existing book When delete cover Then return that book`() {
            whenever(bookRepository.find(bookId)).thenReturn(book)

            val updated = service.delete(bookId)

            assertThat(updated).isSameAs(book)
        }

        @Test
        fun `Given existing book When delete cover Then delegated to repository`() {
            whenever(bookRepository.find(bookId)).thenReturn(book)

            service.delete(bookId)

            verify(coverRepository).delete(bookId)
        }
    }
}
