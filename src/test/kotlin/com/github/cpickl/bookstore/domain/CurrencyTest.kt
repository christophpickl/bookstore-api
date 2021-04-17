package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import org.junit.jupiter.api.Test

class CurrencyTest {
    @Test
    fun `of succeeds`() {
        Currency.values().forEach { currency ->
            assertThat(Currency.of(currency.code)).isEqualTo(currency)
        }
    }

    @Test
    fun `of fails`() {
        assertThat {
            Currency.of("XXX")
        }.isFailure()
    }
}
