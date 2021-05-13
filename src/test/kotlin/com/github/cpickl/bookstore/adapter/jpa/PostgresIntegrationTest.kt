package com.github.cpickl.bookstore.adapter.jpa

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.github.cpickl.bookstore.TestRepositoryCleaner
import com.github.cpickl.bookstore.boundary.any
import com.github.cpickl.bookstore.domain.Book
import com.github.cpickl.bookstore.domain.BookState
import com.github.cpickl.bookstore.domain.CoverImage
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostgresIntegrationTest(
    @Autowired private val userCrudRepo: JpaUserCrudRepository,
    @Autowired private val bookCrudRepo: JpaBookCrudRepository,
    @Autowired private val coverCrudRepo: JpaCoverCrudRepository,
) {

    companion object {
        // keep this one in sync with the one defined in the docker-compose yaml
        private const val postgresDockerImage = "postgres:10.16"
        private val postgres: KPostgreSQLContainer = KPostgreSQLContainer(postgresDockerImage).apply {
            withDatabaseName("testcontainers_db")
            withUsername("user")
            withPassword("pass")
            start() // will automatically shutdown
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.jpa.properties.hibernate.dialect") { CustomPostgreSQLDialect::class.qualifiedName }
        }
    }

    private lateinit var userRepo: JpaUserRepository
    private lateinit var bookRepo: JpaBookRepository
    private lateinit var coverRepo: JpaCoverRepository

    @BeforeEach
    fun `init repos`() {
        userRepo = JpaUserRepository(userCrudRepo)
        bookRepo = JpaBookRepository(bookCrudRepo, userCrudRepo)
        coverRepo = JpaCoverRepository(coverCrudRepo, bookCrudRepo)

        TestRepositoryCleaner(userCrudRepo, bookCrudRepo, coverCrudRepo).deleteAllEntities()
    }

    @Test
    fun `user repo tests`() {
        val user = User.any()

        assertThat(userRepo.isEmpty()).isTrue()
        userRepo.create(user)
        assertThat(userRepo.isEmpty()).isFalse()
        assertThat(userRepo.findById(user.id)).isEqualTo(user)
        assertThat(userRepo.findByUsername(user.username)).isEqualTo(user)
    }

    @Test
    fun `book repo tests`() {
        val user = User.any()
        userRepo.create(user)
        val book = Book.any().copy(author = user.toAuthor(), state = BookState.Published)

        bookRepo.create(book)
        assertThat(bookRepo.findById(book.id)).isEqualTo(book)
        assertThat(bookRepo.findAll()).containsExactly(book)
        val book2 = book.copy(title = "${book.title} updated")
        bookRepo.update(book2)
        assertThat(bookRepo.findById(book.id)).isEqualTo(book2)
    }

    @Test
    fun `cover repo tests`() {
        val user = User.any()
        userRepo.create(user)
        val book = Book.any().copy(author = user.toAuthor())
        bookRepo.create(book)
        val cover = CoverImage.CustomImage.any()

        coverRepo.insertOrUpdate(book.id, cover)
        assertThat(coverRepo.findById(book.id)).isEqualTo(cover)
        coverRepo.delete(book.id)
        assertThat(coverRepo.findById(book.id)).isNull()
    }
}

// java-kotlin workaround
class KPostgreSQLContainer(image: String) : PostgreSQLContainer<KPostgreSQLContainer>(image)
