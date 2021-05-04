package com.github.cpickl.bookstore.domain

open class BookstoreException(message: String) : RuntimeException(message)

class InternalException(message: String) : BookstoreException(message)

class BookNotFoundException(id: Id) : BookstoreException("Book not found by ID: $id")

class UserNotFoundException(id: Id) : BookstoreException("User not found by ID: $id")
