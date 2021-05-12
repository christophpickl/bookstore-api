package com.github.cpickl.bookstore.common

import java.util.EnumSet
import java.util.Enumeration
import java.util.Optional

fun <T> Optional<T>.getOrThrow(exceptionProvider: () -> Exception): T =
    unwrap() ?: throw exceptionProvider()

fun <T> Optional<T>.unwrap(): T? =
    if (isEmpty) null else get()

fun <IN, OUT> Optional<IN>.unwrap(transform: (IN) -> OUT): OUT? =
    if (isEmpty) {
        null
    } else {
        transform(get())
    }

fun <E : Enum<E>> Collection<E>.toEnumSet(): EnumSet<E> = EnumSet.copyOf(this)

fun <E : Enum<E>> enumSetOf(vararg es: E): EnumSet<E> = es.toSet().toEnumSet()

fun <T> List<T>.toEnumeration(): Enumeration<T> =
    object : Enumeration<T> {
        val iterator = this@toEnumeration.iterator()
        override fun hasMoreElements() = iterator.hasNext()
        override fun nextElement() = iterator.next()
    }
