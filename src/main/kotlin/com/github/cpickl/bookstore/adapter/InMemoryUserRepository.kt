package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import mu.KotlinLogging.logger
import org.springframework.stereotype.Repository
import java.lang.IllegalArgumentException

@Repository
class InMemoryUserRepository: UserRepository {

    private val log = logger {}
    private val users = mutableListOf<User>()

    override fun findOrNull(username: String) =
        users.firstOrNull { it.username == username }

    override fun save(user: User) {
        log.debug { "save: $user" }
        users += user
    }
}
