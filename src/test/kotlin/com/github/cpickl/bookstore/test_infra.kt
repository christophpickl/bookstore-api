@file:Suppress("DEPRECATION")

package com.github.cpickl.bookstore

import assertk.Assert
import assertk.Result
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.messageContains
import com.github.cpickl.bookstore.adapter.jpa.allEntities
import com.github.cpickl.bookstore.boundary.LoginDto
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import com.github.cpickl.bookstore.domain.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

inline fun <reified T : Throwable> Assert<Result<Any>>.throws(messageContains: String? = null) {
    val assert = isFailure().isInstanceOf(T::class)
    messageContains?.let {
        assert.messageContains(it)
    }
}


@Service
class TestUserPreparer(
    private val userRepository: UserRepository,
    passwordEncoder: BCryptPasswordEncoder,
) {

    final val userLogin = LoginDto.any()

    val user = User.any().copy(
        username = userLogin.username,
        passwordHash = passwordEncoder.encode(userLogin.password),
    )

    fun saveTestUser() {
        userRepository.create(user)
    }
}

@Repository
class TestRepositoryCleaner(
    @Autowired private val em: EntityManager,
) {
    @Transactional
    fun deleteAllEntities() {
        allEntities.forEach { entity ->
            em.createQuery("DELETE FROM $entity").executeUpdate()
        }
    }
}
