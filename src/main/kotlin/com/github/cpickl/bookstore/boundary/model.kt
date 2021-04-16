package com.github.cpickl.bookstore.boundary

data class BookListDto(
    /** A UUID. */
    val id: String,
    val title: String,
    /** Pseudonym of the published user. */
    val author: String,
    /** Preformatted amount. */
    val price: String, // FUTURE could keep Amount type and add custom jackson serializer
)

data class BookDetailDto(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val author: String,
    val coverLink: String,
)

data class BookCreateDto(
    val title: String,
    val description: String,
    val euroCent: Int,
) {
    companion object
}

data class BookUpdateDto(
    val title: String,
    val description: String,
    val euroCent: Int,
) {
    companion object
}
