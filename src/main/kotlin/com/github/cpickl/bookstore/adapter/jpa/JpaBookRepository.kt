package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookRepository
import com.github.cpickl.bookstore.domain.BookState
import com.github.cpickl.bookstore.domain.Currency
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.Search
import com.github.cpickl.bookstore.domain.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
class JpaBookRepository(
    private val repo: JpaBookCrudRepository
) : BookRepository {

    override fun findAll(search: Search): List<Book> =
        when (search) {
            is Search.Off -> repo.findAllPublished()
            is Search.On -> repo.searchAllPublished(search.term)
        }.map { it.toBook() }


    override fun find(id: Id): Book? =
        repo.findPublished(+id)?.toBook()

    override fun create(book: Book) {
        repo.save(book.toBookJpa())
    }

    override fun update(book: Book) {
        repo.save(book.toBookJpa())
    }

    /*
    override fun findAll(search: Search) = when (search) {
        is Search.Off -> books
        is Search.On -> books.filter { it.title.toLowerCase().contains(search.term) }
    }
        .filter { it.state == BookState.Published }
        .sortedBy { it.title } // FUTURE custom sort order

    override fun find(id: Id) =
        books.firstOrNull { it.id == id && it.state == BookState.Published }

    override fun create(book: Book) {
        log.debug { "create: $book" }
        require(find(book.id) == null) { "duplicate ID: ${book.id}" }
        books += book
    }

    override fun update(book: Book) {
        log.debug { "update: $book" }
        val found = find(book.id) ?: throw IllegalArgumentException("Book not found: ${book.id}")

        require(books.remove(found))
        books += book
    }
     */
}

interface JpaBookCrudRepository : CrudRepository<BookJpa, String> {

    @Query(
        """FROM ${BookJpa.ENTITY_NAME} b 
        WHERE b.state = 'PUBLISHED' AND id=:id"""
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
        WHERE b.state = 'PUBLISHED' AND LOWER(b.title) LIKE CONCAT('%',LOWER(:title),'%')
        ORDER BY b.title ASC"""
    )
    fun searchAllPublished(@Param("title") title: String): List<BookJpa>
}

private fun BookJpa.toBook() = Book(
    id = Id(id),
    title = title,
    description = description,
    author = author.toUser(),
    price = Money(
        currency = Currency.of(currencyCode),
        value = price,
    ),
    state = state.toBookState(),
)

private fun Book.toBookJpa() = BookJpa(
    id = +id,
    title = title,
    description = description,
    author = author.toUserJpa(),
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

private fun UserJpa.toUser() = User(
    id = Id(id),
    authorPseudonym = authorPseudonym,
    username = username,
    passwordHash = passwordHash,
)

private fun User.toUserJpa() = UserJpa(
    id = +id,
    authorPseudonym = authorPseudonym,
    username = username,
    passwordHash = passwordHash,
)
