package com.github.cpickl.bookstore.common

import org.springframework.stereotype.Component
import java.time.LocalDateTime

interface Clock {
    fun now(): LocalDateTime
}

@Component
object SystemClock : Clock {
    override fun now() = LocalDateTime.now()
}
