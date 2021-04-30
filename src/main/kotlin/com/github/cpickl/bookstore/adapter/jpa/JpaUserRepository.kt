package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
class JpaUserRepository(
    private val jpaRepo: JpaUserCrudRepository,
) : UserRepository {

    override fun find(username: String) =
        jpaRepo.findByUsername(username)?.toUser()

    override fun create(user: User) {
        println("save: $user")
        jpaRepo.save(user.toUserJpa())
    }

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
