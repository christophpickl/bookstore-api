package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.UserRepositoryTest

class InMemoryUserRepositoryTest : UserRepositoryTest() {
    override fun testee() = InMemoryUserRepository()
}
