package uz.mkb.auth.integration

import org.junit.jupiter.api.AfterEach
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
import org.springframework.test.web.servlet.get
import uz.mkb.auth.repository.UserRepository
import kotlin.test.assertTrue
import tools.jackson.databind.ObjectMapper

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

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    fun `register user`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON

            content = """
                {
                    "username": "ali",
                    "password": "password123",
                    "fullName": "Test User"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
        }

        assertTrue(userRepository.existsByUsername("ali"))
    }

    @Test
    fun `register, login and me`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """
            {
                "username": "aziz",
                "password": "password123",
                "fullName": "Aziz Valiyev"
            }
        """.trimIndent()
        }.andExpect { status { isCreated() } }

        val result = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON

            content = """
                {
                    "username": "aziz",
                    "password": "password123"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }.andReturn()

        val responseBody = result.response.contentAsString

        val accessToken = objectMapper.readTree(responseBody).get("accessToken").asString()

        mockMvc.get("/api/auth/me") {
            header("Authorization", "Bearer $accessToken")
        }
        .andExpect {
            status { isOk() }
            jsonPath("$.username") { value("aziz") }
        }
    }

    @Test
    fun `me tokensiz 401 qaytarishi kerak`(){
        mockMvc.get("/api/auth/me")
            .andExpect {
                status { isUnauthorized() }
            }

    }

    @Test
    fun `yaroqsiz token 401 qaytarishi kerak`(){
        mockMvc.get("/api/auth/me"){
            header("Authorization","Bearer token")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `invalid password`(){
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "username": "ali",
                    "password": "password123"
                }
            """.trimIndent()
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `dublikat username 409 qaytaradi`() {
        val body = """
        {
            "username": "dublikat",
            "password": "password123",
            "fullName": "Test User"
        }
    """.trimIndent()

        // birinchi register — muvaffaqiyatli
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isCreated() } }

        // ikkinchi marta shu username bilan — 409
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isConflict() }
            jsonPath("$.message") { value("Username dublikat is already taken") }
        }
    }

    @Test
    fun `yaroqsiz refresh token 401 qaytaradi`() {
        mockMvc.post("/api/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"refreshToken": "buYaroqsizToken"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
