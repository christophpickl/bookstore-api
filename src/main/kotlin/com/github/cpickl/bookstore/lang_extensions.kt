package com.github.cpickl.bookstore

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
