package com.github.cpickl.bookstore.domain

open class BookstoreException(message: String, val domainMessage: String) : RuntimeException(message)

class InternalException(message: String) : BookstoreException(message, "Internal error")

class BookNotFoundException(val id: Id) : BookstoreException("Book not found by ID: $id", "Book not found")

class UserNotFoundException(val id: Id) : BookstoreException("User not found by ID: $id", "User not found")

enum class ErrorCode {
    UNKNOWN,
    INVALID_INPUT,
    FORBIDDEN,
    BOOK_NOT_FOUND,
    USER_NOT_FOUND,
}
