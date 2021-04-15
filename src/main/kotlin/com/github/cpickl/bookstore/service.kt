package com.github.cpickl.bookstore

import org.springframework.stereotype.Service

interface BooksService {
    fun getBooks(): List<Book>
}

@Service
class BooksServiceImpl : BooksService {
    private val dummyBooks = listOf(Book(1, "abcc"), Book(2, "deff"))
    override fun getBooks() = dummyBooks
}
