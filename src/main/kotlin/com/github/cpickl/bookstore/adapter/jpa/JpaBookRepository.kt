package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.common.getOrThrow
import com.github.cpickl.bookstore.domain.Author
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookNotFoundException
import com.github.cpickl.bookstore.domain.BookRepository
import com.github.cpickl.bookstore.domain.BookState
import com.github.cpickl.bookstore.domain.Currency
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.Search
import com.github.cpickl.bookstore.domain.UserNotFoundException
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JpaBookRepository(
    private val bookRepo: JpaBookCrudRepository,
    private val userRepo: JpaUserCrudRepository,
) : BookRepository {

    @Transactional(readOnly = true)
    override fun findAll(search: Search): List<Book> =
        when (search) {
            is Search.Off -> bookRepo.findAllPublished()
            is Search.On -> bookRepo.searchAllPublished(search.term)
        }.map { it.toBook() }

    @Transactional(readOnly = true)
    override fun findById(id: Id): Book? =
        bookRepo.findPublished(+id)?.toBook()

    @Transactional
    override fun create(book: Book) {
        val user = findUser(book.author.userId)
        bookRepo.save(book.toBookJpa(user))
    }

    @Transactional
    override fun update(book: Book) {
        if (bookRepo.findById(+book.id).isEmpty) {
            throw BookNotFoundException(book.id)
        }
        val user = findUser(book.author.userId)
        bookRepo.save(book.toBookJpa(user))
    }

    private fun findUser(id: Id) =
        userRepo.findById(+id).getOrThrow { UserNotFoundException(id) }
}

interface JpaBookCrudRepository : CrudRepository<BookJpa, String> {

    @Query(
        """FROM ${BookJpa.ENTITY_NAME} b 
        WHERE b.state = 'PUBLISHED' 
          AND id=:id"""
    )
    fun findPublished(@Param("id") id: String): BookJpa?

    @Query(
        """FROM ${BookJpa.ENTITY_NAME} b
        WHERE b.state = 'PUBLISHED'
        ORDER BY b.title ASC"""
    )
    fun findAllPublished(): List<BookJpa>

    @Query(
        """FROM ${BookJpa.ENTITY_NAME} b
        WHERE b.state = 'PUBLISHED' 
          AND LOWER(b.title) LIKE CONCAT('%',LOWER(:title),'%')
        ORDER BY b.title ASC"""
    )
    fun searchAllPublished(@Param("title") title: String): List<BookJpa>
}

private fun BookJpa.toBook() = Book(
    id = Id(id),
    title = title,
    description = description,
    author = author.toAuthor(),
    price = Money(
        currency = Currency.of(currencyCode),
        value = price,
    ),
    state = state.toBookState(),
)

private fun UserJpa.toAuthor() = Author(
    userId = Id(id),
    pseudonym = authorPseudonym,
)

private fun Book.toBookJpa(user: UserJpa) = BookJpa(
    id = +id,
    title = title,
    description = description,
    author = user,
    currencyCode = price.currency.code,
    price = price.value,
    state = state.toBookStateJpa(),
)

private fun BookStateJpa.toBookState() = when (this) {
    BookStateJpa.UNPUBLISHED -> BookState.Unpublished
    BookStateJpa.PUBLISHED -> BookState.Published
}

private fun BookState.toBookStateJpa() = when (this) {
    BookState.Unpublished -> BookStateJpa.UNPUBLISHED
    BookState.Published -> BookStateJpa.PUBLISHED
}
