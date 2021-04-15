package com.github.cpickl.bookstore

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import mu.KotlinLogging.logger
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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

@Configuration
class JacksonSpringConfig {
    @Bean
    fun jacksonCustomizer() = Jackson2ObjectMapperBuilderCustomizer { jackson ->
        jackson.featuresToDisable(SerializationFeature.INDENT_OUTPUT)
    }
}
