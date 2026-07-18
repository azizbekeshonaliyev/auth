package uz.mkb.auth.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import uz.mkb.auth.model.User
import kotlin.test.assertNull

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `findByUsername mavjud userni qaytaradi`() {
        val user = User(
            username = "ali",
            passwordHash = "hashed",
            fullName = "Ali Valiyev"
        )
        userRepository.save(user)

        val foundUser = userRepository.findByUsername(user.username)

        assertNotNull(foundUser)
        assertEquals(user.username, foundUser.username)
        assertEquals(user.fullName, foundUser.fullName)
    }

    @Test
    fun `findByUsername mavjud bolmagan user uchun null qaytaradi`() {
        val user = userRepository.findByUsername("nonexistent")

        assertNull(user)
    }
}