package com.github.cpickl.bookstore.boundary

import io.swagger.v3.oas.models.Components

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI().components(Components())
        .info(
            Info()
                .title("Bookstore API")
                .version("v1")
                .description("This app provides REST APIs for a book store")
                .contact(
                    Contact()
                        .name("Christoph Pickl")
                        .email("christoph.pickl@gmail.com")
                )
        )
        .servers(
            listOf(
                Server().url("http://localhost:80").description("DEV server"),
                Server().url("http://prod.me").description("PROD server")
            )
        )
}
