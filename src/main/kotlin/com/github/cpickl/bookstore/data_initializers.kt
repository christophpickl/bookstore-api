package com.github.cpickl.bookstore

import com.github.cpickl.bookstore.boundary.SecurityConstants
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.RandomIdGenerator
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import mu.KotlinLogging.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.AbstractEnvironment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class InitialDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
) : ApplicationListener<ApplicationReadyEvent> {

    private val log = logger {}

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (userRepository.isEmpty()) {
            saveAdminUser()
        }
    }

    private fun saveAdminUser() {
        log.info { "Saving admin user." }
        val login = SecurityConstants.admin
        val authorPseudonym = SecurityConstants.adminAuthorName
        val passwordHash = passwordEncoder.encode(login.password)
        val user = User(RandomIdGenerator.generate(), authorPseudonym, login.username, passwordHash)
        userRepository.create(user)
    }
}

@Component
class DummyDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val bookService: BookService,
    private val environment: AbstractEnvironment,
) : ApplicationListener<ApplicationReadyEvent> {

    private val log = logger {}

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (environment.activeProfiles.contains("dummyData")) {
            log.info { "Setting up dummy data." }
            saveDummyUserAndBook()
        }
    }

    private fun saveDummyUserAndBook() {
        val user = User(RandomIdGenerator.generate(), "John Doe", "johnny", passwordEncoder.encode("secret"))
        userRepository.create(user)
        bookService.create(
            BookCreateRequest(
                username = user.username,
                title = "Homo Sapiens",
                description = "A brief history of humankind",
                price = Money.euro(@Suppress("MagicNumber") 42),
            )
        )
    }
}
