package com.github.cpickl.bookstore.adapter.jpa

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookState
import com.github.cpickl.bookstore.domain.Currency
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.Money
import com.github.cpickl.bookstore.domain.Search
import com.github.cpickl.bookstore.domain.UUID1
import com.github.cpickl.bookstore.domain.UUID2
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataJpaTest
class JpaBookRepositoryTest {

    @Autowired
    private lateinit var em: TestEntityManager

    @Autowired
    private lateinit var crudRepo: JpaBookCrudRepository
    private lateinit var repo: JpaBookRepository

    @BeforeEach
    fun `init repo`() {
        repo = JpaBookRepository(crudRepo)
    }

    @Nested
    inner class FindAllTest {

        @Test
        fun `When find all Then return empty`() {
            assertThat(repo.findAll()).isEmpty()
        }

        @Test
        fun `Given unpublished book When find all Then return empty`() {
            persist(BookJpa.any().copy(state = BookStateJpa.UNPUBLISHED))

            val found = repo.findAll()

            assertThat(found).isEmpty()
        }

        @Test
        fun `Given created book When find all Then finds`() {
            val book = persistPublished()

            val found = repo.findAll()

            assertThat(found).containsExactly(book.toBook())
        }

        @Test
        fun `Given two books When find all Then return sorted`() {
            val user = persistUser()
            persistPublished(title = "b", id = UUID1, author = user)
            persistPublished(title = "a", id = UUID2, author = user)

            val found = repo.findAll()

            assertThat(found.map { it.title }).containsExactly("a", "b")
        }

        @Test
        fun `Given book When find all search off Then return single book`() {
            val book = persistPublished()

            val found = repo.findAll(Search.Off)

            assertThat(found).containsExactly(book.toBook())
        }

        @Test
        fun `Given book When search book Then return that book`() {
            val book = persistPublished(title = "xax")

            val found = repo.findAll(Search.On("a"))

            assertThat(found).containsExactly(book.toBook())
        }

        @Test
        fun `Given two books When search books Then return them sorted`() {
            val user = persistUser()
            persistPublished(title = "bx", id = UUID1, author = user)
            persistPublished(title = "ax", id = UUID2, author = user)

            val found = repo.findAll(Search.On("x"))

            assertThat(found.map { it.title }).containsExactly("ax", "bx")
        }

        @Test
        fun `Given unpublished book When search book by correct title Then return nothing`() {
            persist(BookJpa.any().copy(state = BookStateJpa.UNPUBLISHED, title = "a"))

            val found = repo.findAll(Search.On("a"))

            assertThat(found).isEmpty()
        }

        @Test
        fun `Given book When search book lower cased Then return that book`() {
            val book = persistPublished(title = "A")

            val found = repo.findAll(Search.On("a"))

            assertThat(found).containsExactly(book.toBook())
        }

        @Test
        fun `Given book When search book upper cased Then return that book`() {
            val book = persistPublished(title = "a")

            val found = repo.findAll(Search.On("A"))

            assertThat(found).containsExactly(book.toBook())
        }

        @Test
        fun `Given book When search for unknown Then return empty`() {
            persistPublished(title = "a")

            val found = repo.findAll(Search.On("x"))

            assertThat(found).isEmpty()
        }

    }

    @Nested
    inner class FindSingleTest {

        @Test
        fun `When find single Then return null`() {
            val found = repo.find(Id.any())

            assertThat(found).isNull()
        }

        @Test
        fun `Given created book When find single Then finds`() {
            val book = persistPublished()

            val found = repo.find(Id(book.id))

            assertThat(found).isEqualTo(book.toBook())
        }

        @Test
        fun `Given unpublished book When find single Then return null`() {
            val book = persist(BookJpa.any().copy(state = BookStateJpa.UNPUBLISHED))

            val found = repo.find(Id(book.id))

            assertThat(found).isNull()
        }

    }

    @Nested
    inner class CreateTest {
        @Test
        fun `Given book When create with same ID Then fail`() {
            val book = persistPublished()

            assertThat {
                repo.create(Book.any().copy(id = Id(book.id)))
            }.isFailure()
        }
    }

    @Nested
    inner class UpdateTest {
        @Test
        fun `When update non existing Then fail`() {
            assertThat {
                repo.update(Book.any())
            }.isFailure()
        }

        @Test
        fun `Given inserted book When update Then get updated back on find`() {
            val book = persistPublished()
            val updatedBook = book.copy(title = "title2")

            repo.update(updatedBook.toBook())

            assertThat(find(Id(book.id))).isEqualTo(updatedBook)
        }
    }

    private fun persist(book: BookJpa, suppressUserSave: Boolean = false): BookJpa {
        if (!suppressUserSave) {
            em.persistAndFlush(book.author)
        }
        return em.persistAndFlush(book)
    }

    private fun persistPublished(
        id: String = UUID1,
        title: String = "title",
        author: UserJpa? = null,
        suppressUserSave: Boolean = false
    ): BookJpa =
        persist(
            book = BookJpa.any().copy(
                id = id,
                title = title,
                state = BookStateJpa.PUBLISHED
            ).let {
                if (author != null) it.copy(author = author) else it
            },
            suppressUserSave = suppressUserSave,
        )

    private fun find(id: Id): BookJpa? =
        em.find(BookJpa::class.java, +id)

    private fun persistUser(username: String = "username"): UserJpa =
        em.persistAndFlush(UserJpa.any().copy(username = username))
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

private fun BookStateJpa.toBookState() = when (this) {
    BookStateJpa.UNPUBLISHED -> BookState.Unpublished
    BookStateJpa.PUBLISHED -> BookState.Published
}

private fun UserJpa.toUser() = User(
    id = Id(id),
    authorPseudonym = authorPseudonym,
    username = username,
    passwordHash = passwordHash,
)
