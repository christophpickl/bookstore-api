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
class DummyDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val bookService: BookService,
    private val environment: AbstractEnvironment,
) : ApplicationListener<ApplicationReadyEvent> {

    private val log = logger {}

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (!environment.activeProfiles.contains("dummyData")) {
            return
        }
        log.info { "Setting up dummy data." }
        val user = saveDummyUser()
        saveDummyBook(user.username)
    }

    private fun saveDummyUser(): User {
        val login = SecurityConstants.admin
        val authorPseudonym = SecurityConstants.adminAuthorName
        val passwordHash = passwordEncoder.encode(login.password)
        val user = User(RandomIdGenerator.generate(), authorPseudonym, login.username, passwordHash)
        userRepository.create(user)
        return user
    }

    private fun saveDummyBook(username: String) {
        bookService.create(
            BookCreateRequest(
                username = username,
                title = "Homo Sapiens",
                description = "A brief history of humankind",
                price = Money.euro(@Suppress("MagicNumber") 42),
            )
        )
    }
}
