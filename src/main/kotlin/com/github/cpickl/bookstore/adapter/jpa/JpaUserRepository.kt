package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import com.github.cpickl.bookstore.unwrap
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
class JpaUserRepository(
    private val repo: JpaUserCrudRepository,
) : UserRepository {

    override fun findById(id: Id): User? {
        return repo.findById(+id).unwrap { it.toUser() }
    }

    override fun findByUsername(username: String) =
        repo.findByUsername(username)?.toUser()

    override fun create(user: User) {
        repo.save(user.toUserJpa())
    }

    override fun isEmpty() =
        repo.count() == 0L

    private fun User.toUserJpa() = UserJpa(
        id = +id,
        authorPseudonym = authorPseudonym,
        username = username,
        passwordHash = passwordHash,
    )

    private fun UserJpa.toUser() = User(
        id = Id(id),
        authorPseudonym = authorPseudonym,
        username = username,
        passwordHash = passwordHash,
    )
}

interface JpaUserCrudRepository : CrudRepository<UserJpa, String> {
    fun findByUsername(username: String): UserJpa?
}
