package com.github.cpickl.bookstore.boundary

import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonSpringConfig {
    @Bean
    fun jacksonCustomizer() = Jackson2ObjectMapperBuilderCustomizer { jackson ->
        jackson.featuresToDisable(SerializationFeature.INDENT_OUTPUT)
    }
}
