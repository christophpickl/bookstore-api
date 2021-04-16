package com.github.cpickl.bookstore

import com.github.cpickl.bookstore.boundary.LoginDto
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import com.github.cpickl.bookstore.domain.any
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

val UUID1: UUID = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")

@Service
class UserTestPreparer(
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
