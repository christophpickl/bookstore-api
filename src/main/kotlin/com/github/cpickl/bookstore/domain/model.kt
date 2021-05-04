package com.github.cpickl.bookstore.domain

data class User(
    val id: Id,
    val authorPseudonym: String,
    val username: String, // FUTURE could be custom data type
    val passwordHash: String,
) {
    companion object

    fun toAuthor() = Author(
        userId = id,
        pseudonym = authorPseudonym,
    )

    override fun toString() = "User[username='$username',id=$id,authorPseudonym='$authorPseudonym']"
}

data class Author(
    val userId: Id,
    val pseudonym: String,
) {
    companion object
}

data class Book(
    val id: Id,
    val title: String,
    val description: String,
    val author: Author,
    val price: Money,
    val state: BookState,
) {
    companion object

    val authorName = author.pseudonym
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
    ) : CoverImage(bytes) {

        companion object;

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CustomImage) return false
            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode() = bytes.hashCode()
        override fun toString() = "CustomImage[bytes.size=${bytes.size}]"
    }
}

data class CoverUpdateRequest(
    val bytes: ByteArray,
) {
    companion object;

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoverUpdateRequest

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode() = bytes.contentHashCode()
}
