package com.github.cpickl.bookstore

data class Book(
    val id: Int,
    val title: String,
) {
    companion object // for test extensions
}
