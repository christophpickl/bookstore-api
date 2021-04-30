package com.github.cpickl.bookstore.adapter.jpa

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

@Entity
@Table(name = UserJpa.TABLE_NAME)
data class UserJpa(
    @Id
    @Column(name = "id", unique = true)
    val id: String,
    @Column(name = "author_pseudonym")
    val authorPseudonym: String,
    @Column(name = "username", unique = true)
    val username: String,
    @Column(name = "password_hash")
    val passwordHash: String,
) {
    companion object {
        const val TABLE_NAME = "user"
    }
}

@Entity
@Table(name = BookJpa.TABLE_NAME)
data class BookJpa(
    @Id
    @Column(name = "id", unique = true)
    val id: String,
    @Column(name = "title")
    val title: String,
    @Column(name = "description")
    @Lob
    val description: String,
    // @ManyToOne
//    var author: User,
//    var price: Money,
//    @Enumerated(value = EnumType.STRING)
//    var state: BookStateJpa,
) {
    companion object {
        const val TABLE_NAME = "book"
    }
}

//enum class BookStateJpa {
//}


@Entity
@Table(name = CoverJpa.TABLE_NAME)
data class CoverJpa(
    @Id
    @Column(name = "book_id", unique = true)
    val bookId: String,

    @Column(name = "bytes")
    @Lob
    val bytes: ByteArray,
) {
    companion object {
        const val TABLE_NAME = "cover"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoverJpa) return false
        return bookId == other.bookId &&
                bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = bookId.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
