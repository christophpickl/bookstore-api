package com.github.cpickl.bookstore.domain

import org.springframework.stereotype.Service

interface BooksService {
    fun getBooks(): List<Book>
}

@Service
class BooksServiceImpl(
    private val repository: BooksRepository
) : BooksService {

    override fun getBooks() = repository.findAll()

}
