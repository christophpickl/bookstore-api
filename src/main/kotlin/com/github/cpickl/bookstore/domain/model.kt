package com.github.cpickl.bookstore.domain

data class User(
    val id: Id,
    val authorPseudonym: String,
    val username: String, // FUTURE could be custom data type
    val passwordHash: String,
) {
    companion object;

    override fun toString() = "User[username='$username',id=$id,authorPseudonym='$authorPseudonym']"
}

data class Book(
    val id: Id,
    val title: String,
    val description: String,
    val author: User,
    val price: Money,
    val state: BookState,
) {
    companion object

    val authorName = author.authorPseudonym
}

enum class BookState {
    Unpublished,
    Published;

    companion object
}

data class BookCreateRequest(
    val username: String,
    val title: String,
    val description: String,
    val price: Money,
) {
    companion object
}

data class BookUpdateRequest(
    val username: String,
    val id: Id,
    val title: String,
    val description: String,
    val price: Money,
)

sealed class CoverImage(
    val bytes: ByteArray,
) {
    companion object

    object DefaultImage : CoverImage(
        CoverImage::class.java
            .getResourceAsStream("/bookstore/icon_default_book.png")!!
            .readAllBytes()!!
    )

    class CustomImage(
        bytes: ByteArray,
    ) : CoverImage(bytes)
}
