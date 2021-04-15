package com.github.cpickl.bookstore

import com.github.cpickl.bookstore.boundary.SecurityConfig
import com.github.cpickl.bookstore.domain.*
import mu.KotlinLogging.logger
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.core.env.AbstractEnvironment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

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

const val PROFILE_DUMMY_DATA = "dummyData"

@Component
class SetupDummyUser(
    private val userRepository: UserRepository,
    private val encoder: BCryptPasswordEncoder,
    private val bookRepository: BookRepository,
    private val environment: AbstractEnvironment,
) : ApplicationListener<ApplicationReadyEvent> {

    private val log = logger {}
    private val book = Book(
        id = UUID.randomUUID(),
        title = "Homo Sapiens",
        description = "A brief history of humankind",
        author = User(UUID.randomUUID(), "Harari", "username", "123hash"),
        cover = Image(UUID.randomUUID(), byteArrayOf(0, 1)),
        price = Amount.euro(42),
    )

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (!environment.activeProfiles.contains(PROFILE_DUMMY_DATA)) {
            return
        }
        log.info { "Setting up dummy data." }
        val user = SecurityConfig.admin
        val id = UUID.randomUUID()
        val authorPseudonym = SecurityConfig.adminAuthorName
        val passwordHash = encoder.encode(user.password)
        userRepository.save(User(id, authorPseudonym, user.username, passwordHash))

        bookRepository.save(book)
    }

}