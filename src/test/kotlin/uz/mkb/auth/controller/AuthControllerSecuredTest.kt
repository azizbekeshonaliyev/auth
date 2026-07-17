package uz.mkb.auth.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uz.mkb.auth.config.SecurityConfig
import uz.mkb.auth.dto.UserResponse
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
            classes = [SecurityConfig::class, JwtAuthFilter::class],
        ),
    ],
)
class AuthControllerSecuredTest {

    @Autowired
    lateinit var mvc: MockMvc

    @MockkBean
    lateinit var authService: AuthService


    @Nested
    inner class Me {

        @Test
        fun `me 200`(){
            val userResponse = UserResponse(
                id = 1L,
                username = "ali",
                fullName = "Test User",
                role = "USER",
            )
            every { authService.getCurrentUser(any())} returns userResponse

            mvc.get("/api/auth/me"){
                with(user("ali"))
            }.andDo {
                print()
            }.andExpect {
                status { isOk() }

                jsonPath("$.id") { value(1) }
                jsonPath("$.username") { value("ali") }
            }
        }
    }
}