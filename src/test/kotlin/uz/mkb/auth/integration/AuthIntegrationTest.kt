package uz.mkb.auth.integration

import org.springframework.http.MediaType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import uz.mkb.auth.repository.UserRepository
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        var postgres = PostgreSQLContainer("postgres:16-alpine")

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `register user`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON

            content = """
                {
                    "username": "testuser",
                    "password": "password123",
                    "fullName": "Test User"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
        }

        assertTrue(userRepository.existsByUsername("testuser"))
    }
}
