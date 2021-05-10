package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.domain.Roles
import java.util.EnumSet
import javax.persistence.CascadeType
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
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

    @ManyToOne(cascade = [CascadeType.REMOVE])
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
    // TODO setup foreign key reference to Book
    @Id
    @Column(name = "book_id", unique = true, nullable = false)
    val bookId: String,

    @Lob
    @Column(name = "bytes")
    val bytes: ByteArray,
) {

    companion object {
        const val ENTITY_NAME = "Cover"
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
