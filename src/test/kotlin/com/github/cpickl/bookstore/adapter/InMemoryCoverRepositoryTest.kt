package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.CoverRepositoryTest

class InMemoryCoverRepositoryTest : CoverRepositoryTest() {
    override fun testee() = InMemoryCoverRepository()
}
