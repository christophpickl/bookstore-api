@file:Suppress("DEPRECATION")

package com.github.cpickl.bookstore

import assertk.Assert
import assertk.Result
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.messageContains

inline fun <reified T : Throwable> Assert<Result<Any>>.throws(messageContains: String? = null) {
    val assert = isFailure().isInstanceOf(T::class)
    messageContains?.let {
        assert.messageContains(it)
    }
}
