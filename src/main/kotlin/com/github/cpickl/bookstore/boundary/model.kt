package com.github.cpickl.bookstore.boundary

data class BookListDto(
    val id: Int,
    val title: String,
    val author: String,
    val price: String, // FUTURE could keep Amount type and add custom jackson serializer
)
