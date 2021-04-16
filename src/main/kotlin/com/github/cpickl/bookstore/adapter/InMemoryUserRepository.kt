package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import mu.KotlinLogging.logger
import org.springframework.stereotype.Repository

@Repository
class InMemoryUserRepository: UserRepository {

    private val log = logger {}
    private val users = mutableListOf<User>()

    override fun findOrNull(username: String) =
        users.firstOrNull { it.username == username }

    override fun create(user: User) {
        log.debug { "create: $user" }
        users += user
    }
}
