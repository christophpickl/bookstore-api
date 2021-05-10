package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.domain.Roles
import javax.persistence.CascadeType
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity(name = UserJpa.ENTITY_NAME)
@Table(name = UserJpa.TABLE_NAME)
data class UserJpa(
    @Id
    @Column(name = "id", unique = true, nullable = false)
    val id: String,

    @Column(name = "author_pseudonym")
    val authorPseudonym: String,

    @Column(name = "username", unique = true)
    val username: String,

    @Column(name = "password_hash")
    val passwordHash: String,

    @Column(name = "roles", nullable = false)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @ElementCollection(targetClass = RoleJpa::class, fetch = FetchType.EAGER)
    val roles: Set<RoleJpa>,
) {
    companion object {
        const val ENTITY_NAME = "User"
        const val TABLE_NAME = "user"
    }
}

enum class RoleJpa(val roleName: String) {
    USER(Roles.user), ADMIN(Roles.admin)
}

@Entity(name = BookJpa.ENTITY_NAME)
@Table(name = BookJpa.TABLE_NAME)
data class BookJpa(
    @Id
    @Column(name = "id", unique = true, nullable = false)
    val id: String,

    @Column(name = "title")
    val title: String,

    @Lob
    @Column(name = "description")
    val description: String,

    @ManyToOne(cascade = [CascadeType.ALL])
    val author: UserJpa,

    @Column(name = "currency_code")
    val currencyCode: String,

    @Column(name = "price")
    val price: Int,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "state")
    var state: BookStateJpa,
) {
    companion object {
        const val ENTITY_NAME = "Book"
        const val TABLE_NAME = "book"
    }
}

enum class BookStateJpa {
    UNPUBLISHED,
    PUBLISHED;
}

@Entity(name = CoverJpa.ENTITY_NAME)
@Table(name = CoverJpa.TABLE_NAME)
data class CoverJpa(
    @Id
    @Column(name = "id", unique = true, nullable = false)
    val bookId: String,

//    @OneToOne(cascade = [CascadeType.ALL], optional = false)
//    @PrimaryKeyJoinColumn//(name = "book_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    val book: BookJpa,

    @Lob
    @Column(name = "bytes")
    val bytes: ByteArray,
) {

    constructor(book: BookJpa, bytes: ByteArray) : this(book.id, book, bytes)

    companion object {
        const val ENTITY_NAME = "Cover"
        const val TABLE_NAME = "cover"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoverJpa) return false
        return book == other.book &&
                bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
