package uz.mkb.auth.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.springframework.http.MediaType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uz.mkb.auth.config.SecurityConfig
import uz.mkb.auth.dto.AuthResponse
import uz.mkb.auth.dto.UserResponse
import uz.mkb.auth.model.Role
import uz.mkb.auth.security.JwtAuthFilter
import uz.mkb.auth.service.AuthService

@WebMvcTest(
    controllers = [AuthController::class],

    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        SecurityFilterAutoConfiguration::class,
        UserDetailsServiceAutoConfiguration::class,
    ],

    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class, JwtAuthFilter::class]
        )
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @MockkBean
    lateinit var authService: AuthService

    @Nested
    inner class Register {
        // Test cases for register endpoint

        @Test
        fun `register 201 va token qaytaradi`() {
            // Implement test logic here

            //given
            val authResponse = AuthResponse(
                accessToken = "token",
                refreshToken = "refresh token",
                expiresIn = 3600,
            )
            every { authService.register(any()) } returns authResponse

            //when
            mvc.post("/api/auth/register") {
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
                jsonPath("$.accessToken") { value("token") }
                jsonPath("$.refreshToken") { value("refresh token") }
            }

            verify{ authService.register(any()) }
        }

        @Test
        fun `register qisqa parol 400`() {
            mvc.post("/api/auth/register") {
                contentType = MediaType.APPLICATION_JSON

                content = """
                    {
                    "username": "testuser",
                    "password": "123",
                    "fullName": "Test User"
                    }
                """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }

    @Nested
    inner class Login {

        @Test
        fun `login 200 va token qaytaradi`() {
            val authResponse = AuthResponse(
                accessToken = "token",
                refreshToken = "refresh token",
                expiresIn = 3600,
            )

            every { authService.login(any()) } returns authResponse

            mvc.post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON

                content = """
                    {
                    "username": "testuser",
                    "password": "password123"
                    }
                """.trimIndent()
            }.andExpect {
                status { isOk() }
                jsonPath("$.accessToken") { value("token") }
                jsonPath("$.refreshToken") { value("refresh token") }
            }
        }

        @Test
        fun `login 400 va token qaytaradi`() {

            mvc.post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON

                content = """
                  {
                  "username": "",
                  "password": ""
                  }
                """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }
}