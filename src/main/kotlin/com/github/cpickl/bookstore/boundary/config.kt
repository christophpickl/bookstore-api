package com.github.cpickl.bookstore.boundary

import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class JacksonSpringConfig {

    @Bean
    fun jacksonCustomizer() = Jackson2ObjectMapperBuilderCustomizer { jackson ->
        jackson.featuresToDisable(SerializationFeature.INDENT_OUTPUT)
    }

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

}
