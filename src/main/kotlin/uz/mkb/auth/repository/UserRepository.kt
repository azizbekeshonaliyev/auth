package uz.mkb.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import uz.mkb.auth.model.User

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User

    fun existsByUsername(username: String): Boolean
}