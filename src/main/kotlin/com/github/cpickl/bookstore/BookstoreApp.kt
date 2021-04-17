package com.github.cpickl.bookstore

import com.github.cpickl.bookstore.boundary.SecurityConstants
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookRepository
import com.github.cpickl.bookstore.domain.BookState
import com.github.cpickl.bookstore.domain.Image
import com.github.cpickl.bookstore.domain.RandomIdGenerator
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import mu.KotlinLogging.logger
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.core.env.AbstractEnvironment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@SpringBootApplication
class BookstoreApp {
    companion object {
        private val log = logger {}

        @JvmStatic
        fun main(args: Array<String>) {
            log.info { "bookstore starting ..." }
            @Suppress("SpreadOperator")
            runApplication<BookstoreApp>(*args) {
                setBannerMode(Banner.Mode.OFF)
            }
        }
    }

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()
}

const val PROFILE_DUMMY_DATA = "dummyData"

@Component
class SetupDummyUser(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val bookRepository: BookRepository,
    private val environment: AbstractEnvironment,
) : ApplicationListener<ApplicationReadyEvent> {

    private val log = logger {}
    private val book = Book(
        id = RandomIdGenerator.generate(),
        title = "Homo Sapiens",
        description = "A brief history of humankind",
        author = User(RandomIdGenerator.generate(), "Harari", "username", "123hash"),
        cover =Image.empty(),
        price = Money.euro(@Suppress("MagicNumber") 42),
        state = BookState.Published,
    )

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (!environment.activeProfiles.contains(PROFILE_DUMMY_DATA)) {
            return
        }
        log.info { "Setting up dummy data." }
        val user = SecurityConstants.admin
        val id = RandomIdGenerator.generate()
        val authorPseudonym = SecurityConstants.adminAuthorName
        val passwordHash = passwordEncoder.encode(user.password)
        userRepository.create(User(id, authorPseudonym, user.username, passwordHash))

        bookRepository.create(book)
    }

}
