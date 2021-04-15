package com.github.cpickl.bookstore

import mu.KotlinLogging.logger
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookstoreApp {
    companion object {
        private val log = logger {}
        @JvmStatic
        fun main(args: Array<String>) {
            log.info { "bookstore starting ..." }
            runApplication<BookstoreApp>(*args) {
                setBannerMode(Banner.Mode.OFF)
            }
        }
    }
}
