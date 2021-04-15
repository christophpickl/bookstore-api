package com.github.cpickl.bookstore.adapter

import com.github.cpickl.bookstore.domain.*
import org.springframework.stereotype.Repository

@Repository
class InMemoryBooksRepository : BooksRepository {
    private val books = mutableListOf(
        Book(
            id = 1,
            title = "Homo Sapiens",
            description = "A brief history of humankind",
            author = User("Harari", "username", "123hash"),
            coverImage = Image("image ID", byteArrayOf(0, 1)),
            price = Amount.euro(42),
        )
    )

    override fun findAll() = books
}
