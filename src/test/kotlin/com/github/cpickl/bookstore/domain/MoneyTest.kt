package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

class MoneyTest {

    private val currencyCode = "CUR"

    private val amountsToFormat = Stream.of(
        Arguments.of(1, 0, "1"),
        Arguments.of(1, 1, "0.1"),
        Arguments.of(10, 1, "1.0"),
        Arguments.of(1, 2, "0.01"),
        Arguments.of(10, 2, "0.10"),
        Arguments.of(100, 2, "1.00"),
    )

    @Test
    fun `Given each amount When format Then format expectedly`() {
        amountsToFormat.forEach {
            val (value, precision, expected) = it.get()

            assertThat(Money.format(currencyCode, value as Int, precision as Int))
                .isEqualTo("$currencyCode $expected")
        }
    }

    @Test
    fun `money factories`() {
        assertThat(Money.euro(1)).isEqualTo(Money(Currency.Euro, 100))
        assertThat(Money.euroCent(1)).isEqualTo(Money(Currency.Euro, 1))
    }
}
