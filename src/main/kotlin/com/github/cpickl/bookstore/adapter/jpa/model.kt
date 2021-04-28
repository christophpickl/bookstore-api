package com.github.cpickl.bookstore.adapter.jpa

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

@Entity
@Table(name = "user")
data class UserJpa(
    @Id
    val id: String,
    @Column
    val authorPseudonym: String,
    @Column
    val username: String,
    @Column
    val passwordHash: String,
) {
    companion object
}

@Entity
@Table(name = "book")
data class BookJpa(
    @Id
    val id: String,
    @Column
    val title: String,
    @Column
    @Lob
    val description: String,
    // @ManyToOne
//    var author: User,
//    var price: Money,
//    @Enumerated(value = EnumType.STRING)
//    var state: BookStateJpa,
)

//enum class BookStateJpa {
//}
