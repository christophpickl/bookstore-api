package com.github.cpickl.bookstore

import com.github.cpickl.bookstore.boundary.SecurityConstants
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookRepository
import com.github.cpickl.bookstore.domain.BookState
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.RandomIdGenerator
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.AbstractEnvironment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DummyDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val bookRepository: BookRepository,
    private val environment: AbstractEnvironment,
) : ApplicationListener<ApplicationReadyEvent> {

    private val log = KotlinLogging.logger {}

    private val book = Book(
        id = Id(UUID.fromString("00000000-1111-2222-3333-444444444444")),
        title = "Homo Sapiens",
        description = "A brief history of humankind",
        author = User(RandomIdGenerator.generate(), "Harari", "username", "123hash"),
        price = Money.euro(@Suppress("MagicNumber") 42),
        state = BookState.Published,
    )

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (!environment.activeProfiles.contains("dummyData")) {
            return
        }
        log.info { "Setting up dummy data." }
        val user = SecurityConstants.admin
        val authorPseudonym = SecurityConstants.adminAuthorName
        val passwordHash = passwordEncoder.encode(user.password)
        userRepository.create(User(RandomIdGenerator.generate(), authorPseudonym, user.username, passwordHash))

        bookRepository.create(book)
    }
}