package com.github.cpickl.bookstore

import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
@EnableAutoConfiguration(
    exclude = [
        ErrorMvcAutoConfiguration::class // disable whitelabel error page
    ]
)
class BookstoreApp {
    companion object {
        private val log = logger {}

        @JvmStatic
        fun main(args: Array<String>) {
            log.info { "bookstore starting ..." }
            log.info { ASCII_LOGO }
            @Suppress("SpreadOperator")
            runApplication<BookstoreApp>(*args) {
                setBannerMode(Banner.Mode.OFF)
            }
        }
    }

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

}

@Configuration
class WebConfig : WebMvcConfigurer {

    @Autowired
    private lateinit var logInterceptor: LogGetRequestsInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(logInterceptor)
    }
}

private const val ASCII_LOGO = """
.----.  .----.  .----. .-. .-. .----..---.  .----. .----. .----.
| {}  }/  {}  \/  {}  \| |/ / { {__ {_   _}/  {}  \| {}  }| {_  
| {}  }\      /\      /| |\ \ .-._} } | |  \      /| .-. \| {__ 
`----'  `----'  `----' `-' `-'`----'  `-'   `----' `-' `-'`----'
"""
