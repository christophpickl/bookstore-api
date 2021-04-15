package com.github.cpickl.bookstore

import org.springframework.stereotype.Service

interface BooksService {
    fun getBooks(): List<Book>
}

@Service
class BooksServiceImpl : BooksService {
    private val dummyBooks = listOf(
        Book(
            id = 1,
            title = "Homo Sapiens",
            description = "A brief history of humankind",
            author = User("Harari", "username", "123hash"),
            coverImage = Image("image ID", byteArrayOf(0, 1)),
            price = Amount.euro(42),
        )
    )

    override fun getBooks() = dummyBooks
}
