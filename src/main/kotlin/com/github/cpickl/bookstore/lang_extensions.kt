package com.github.cpickl.bookstore

import java.util.Optional

fun <IN, OUT> Optional<IN>.unwrap(transform: (IN) -> OUT): OUT? =
    if (isEmpty) {
        null
    } else {
        transform(get())
    }
