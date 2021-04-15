package com.github.cpickl.bookstore.domain

interface BooksRepository {
    fun findAll(): List<Book>
}