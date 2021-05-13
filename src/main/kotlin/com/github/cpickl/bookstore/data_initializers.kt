package com.github.cpickl.bookstore

import com.github.cpickl.bookstore.common.enumSetOf
import com.github.cpickl.bookstore.domain.BookCreateRequest
import com.github.cpickl.bookstore.domain.BookService
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.RandomIdGenerator
import com.github.cpickl.bookstore.domain.Role
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.AbstractEnvironment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class InitialDataInitializer(
    private val userRepo: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val dummyDataInitializer: DummyDataInitializer,
    @Value("\${bookstore.adminDefaultPassword}") private val adminDefaultPassword: String,
) : ApplicationListener<ApplicationReadyEvent> {

    private val log = logger {}

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (userRepo.isEmpty()) {
            saveAdminUser()
        }
        dummyDataInitializer.initialize()
    }

    private fun saveAdminUser() {
        log.info { "Saving default admin user." }
        val passwordHash = passwordEncoder.encode(adminDefaultPassword)
        val user = User(
            id = RandomIdGenerator.generate(),
            username = "admin",
            passwordHash = passwordHash,
            authorPseudonym = "admin author",
            roles = enumSetOf(Role.User, Role.Admin),
        )
        userRepo.create(user)
    }
}

@Component
class DummyDataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val bookService: BookService,
    private val environment: AbstractEnvironment,
) {

    private val log = logger {}

    fun initialize() {
        if (environment.activeProfiles.contains("insertDummyData")) {
            log.info { "Inserting dummy data." }
            saveDummyUserAndBook()
        }
    }

    private fun saveDummyUserAndBook() {
        val user = User(
            id = RandomIdGenerator.generate(),
            authorPseudonym = "John Doe",
            username = "johnny",
            passwordHash = passwordEncoder.encode("secret"),
            roles = enumSetOf(Role.User),
        )
        userRepository.create(user)

        saveBook(
            user,
            title = "Homo Sapiens",
            description = "A brief history of humankind",
            priceInEuroCents = 42_00,
        )
        saveBook(
            user,
            title = "Animal Farm",
            description = "We are all equal, but some are more equal than others",
            priceInEuroCents = 9_80,
        )
        saveBook(
            user,
            title = "Robin",
            description = "A cheesy novel about Positive Feeling",
            priceInEuroCents = 12_00,
        )
    }

    private fun saveBook(user: User, title: String, description: String, priceInEuroCents: Int) {
        bookService.create(
            BookCreateRequest(
                username = user.username,
                title = title,
                description = description,
                price = Money.euroCent(priceInEuroCents),
            )
        )
    }
}
