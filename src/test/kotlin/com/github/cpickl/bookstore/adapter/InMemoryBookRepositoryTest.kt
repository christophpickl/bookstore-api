package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.BookRepositoryTest

class InMemoryBookRepositoryTest : BookRepositoryTest() {
    override fun testee() = InMemoryBookRepository()
}
