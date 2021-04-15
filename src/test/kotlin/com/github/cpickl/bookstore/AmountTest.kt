package com.github.cpickl.bookstore

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

class AmountTest {

    private val anyCurrency = Currency.any()

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
            val amount = Amount(anyCurrency, value as Int, precision as Int)

            assertThat(amount.formatted).isEqualTo("${anyCurrency.code} $expected")
        }
    }
}
