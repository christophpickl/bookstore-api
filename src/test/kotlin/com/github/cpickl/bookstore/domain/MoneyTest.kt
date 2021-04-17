package com.github.cpickl.bookstore.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

class MoneyTest {
    @Test
    fun `money factories`() {
        assertThat(Money.euro(1)).isEqualTo(Money(Currency.Euro, 100))
        assertThat(Money.euroCent(1)).isEqualTo(Money(Currency.Euro, 1))
    }
}
