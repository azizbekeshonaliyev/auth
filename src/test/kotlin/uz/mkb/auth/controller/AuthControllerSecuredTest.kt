package uz.mkb.auth.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uz.mkb.auth.dto.UserResponse
import uz.mkb.auth.service.AuthService

@WebMvcTestWithoutSecurity(AuthController::class)
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