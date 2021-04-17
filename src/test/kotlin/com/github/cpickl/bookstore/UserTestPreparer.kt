package com.github.cpickl.bookstore

import com.github.cpickl.bookstore.boundary.LoginDto
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import com.github.cpickl.bookstore.domain.any
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

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
