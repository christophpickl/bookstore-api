package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.github.cpickl.bookstore.boundary.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class CoverRepositoryTest {

    private lateinit var testee: CoverRepository
    private val id = Id.any()
    private val image = CoverImage.CustomImage.any()

    abstract fun testee(): CoverRepository

    @BeforeEach
    fun `init testee`() {
        testee = testee()
    }

    @Test
    fun `When find unknown Then return null`() {
        val found = testee.findOrNull(Id.any())

        assertThat(found).isNull()
    }

    @Test
    fun `Given custom image When find it Then return it`() {
        testee.update(id, image)

        assertThat(testee.findOrNull(id)).isEqualTo(image)
    }
}
