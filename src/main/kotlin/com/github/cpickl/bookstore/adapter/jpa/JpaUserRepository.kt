package com.github.cpickl.bookstore.adapter.jpa

import com.github.cpickl.bookstore.common.toEnumSet
import com.github.cpickl.bookstore.common.unwrap
import com.github.cpickl.bookstore.domain.Id
import com.github.cpickl.bookstore.domain.InternalException
import com.github.cpickl.bookstore.domain.Role
import com.github.cpickl.bookstore.domain.User
import com.github.cpickl.bookstore.domain.UserRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.EnumSet

@Repository
class JpaUserRepository(
    private val userRepo: JpaUserCrudRepository,
) : UserRepository {

    override fun findById(id: Id): User? =
        userRepo.findById(+id).unwrap { it.toUser() }

    override fun findByUsername(username: String) =
        userRepo.findByUsername(username)?.toUser()

    override fun create(user: User) {
        userRepo.save(user.toUserJpa())
    }

    override fun isEmpty() =
        userRepo.count() == 0L

}

private fun User.toUserJpa() = UserJpa(
    id = +id,
    authorPseudonym = authorPseudonym,
    username = username,
    passwordHash = passwordHash,
    roles = roles.map { it.toRoleJpa() }.toEnumSet()
)

private fun UserJpa.toUser() = User(
    id = Id(id),
    authorPseudonym = authorPseudonym,
    username = username,
    passwordHash = passwordHash,
    roles = roles.map { Role.byName(it.roleName) }.toEnumSet()
)

private fun Role.toRoleJpa() = when(this) {
    Role.User -> RoleJpa.USER
    Role.Admin -> RoleJpa.ADMIN
}

interface JpaUserCrudRepository : CrudRepository<UserJpa, String> {
    fun findByUsername(username: String): UserJpa?
}
