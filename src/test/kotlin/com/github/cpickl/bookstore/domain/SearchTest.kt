package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import org.junit.jupiter.api.Test

class SearchTest {

    @Test
    fun `search term implicitly lower cased`() {
        assertThat(Search.On("UPPER").term).isEqualTo("upper")
    }

    @Test
    fun `invalid search term fails`() {
        assertThat {
            Search.On(" ")
        }.isFailure()
        assertThat {
            Search.On("")
        }.isFailure()
    }
}
