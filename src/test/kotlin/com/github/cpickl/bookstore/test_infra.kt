package com.github.cpickl.bookstore

import assertk.Assert
import assertk.Result
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.messageContains
import com.github.cpickl.bookstore.adapter.jpa.JpaBookCrudRepository
import com.github.cpickl.bookstore.adapter.jpa.JpaCoverCrudRepository
import com.github.cpickl.bookstore.adapter.jpa.JpaUserCrudRepository
import com.github.cpickl.bookstore.boundary.LoginDto
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.common.enumSetOf
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Role
import com.github.cpickl.bookstore.domain.UUID1
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Suppress("DEPRECATION")
inline fun <reified T : Throwable> Assert<Result<Any>>.throws(messageContains: String? = null) {
    val assert = isFailure().isInstanceOf(T::class)
    messageContains?.let {
        assert.messageContains(it)
    }
}

@Service
class TestUserPreparer(
    private val userRepo: UserRepository,
    passwordEncoder: BCryptPasswordEncoder,
) {

    final val userLogin = LoginDto.any().copy(username = "testUser")

    val user = User(
        id = Id(UUID1),
        username = userLogin.username,
        passwordHash = passwordEncoder.encode(userLogin.password),
        authorPseudonym = "test user pseudonym",
        roles = enumSetOf(Role.User),
    )

    fun saveTestUser() {
        userRepo.create(user)
    }
}

@Repository
class TestRepositoryCleaner(
    private val userRepo: JpaUserCrudRepository,
    private val bookRepo: JpaBookCrudRepository,
    private val coverRepo: JpaCoverCrudRepository,
) {
    @Transactional
    fun deleteAllEntities() {
        // order is of relevance!
        coverRepo.deleteAll()
        bookRepo.deleteAll()
        userRepo.deleteAll()
    }
}
