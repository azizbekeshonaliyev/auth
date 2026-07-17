package uz.mkb.auth.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import uz.mkb.auth.dto.LoginRequest
import uz.mkb.auth.dto.RefreshRequest
import uz.mkb.auth.dto.RegisterRequest
import uz.mkb.auth.exception.InvalidCredentialsException
import uz.mkb.auth.exception.InvalidTokenException
import uz.mkb.auth.exception.UsernameAlreadyExistsException
import uz.mkb.auth.model.User
import uz.mkb.auth.repository.UserRepository
import uz.mkb.auth.security.JwtUtil
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val passwordEncoder = mockk<PasswordEncoder>(relaxed = true)
    private val authenticationManager = mockk<AuthenticationManager>(relaxed = true)
    private val jwtUtil = mockk<JwtUtil>()

    private val authService = AuthService(userRepository, passwordEncoder, authenticationManager, jwtUtil)

    @BeforeEach
    fun setUp() {
        every { jwtUtil.generateAccessToken(any(), any()) } returns "access token"
        every { jwtUtil.generateRefreshToken(any()) } returns "refresh token"
        every { jwtUtil.accessTokenExpirationMs } returns 3600000
    }

    @Nested
    inner class Register {
        @Test
        fun `Register muvaffaqiyatli otishi`(){

            // Given

            val request = RegisterRequest(
                fullName = "Ali Valiyev",
                username = "ali",
                password = "parol123",
            )

            every { userRepository.existsByUsername("ali") } returns false
            every { passwordEncoder.encode("parol123") } returns "hashed"
            every { userRepository.save(any()) } answers { firstArg() }

            // when
            val response = authService.register(request)

            // then
            assertEquals("access token", response.accessToken)
            assertEquals("refresh token", response.refreshToken)
            verify { userRepository.save(any()) }
        }

        @Test
        fun `Username already exists`(){

            // Given

            val request = RegisterRequest(
                fullName = "Asad",
                username = "asad",
                password = "asad",
            )

            every { userRepository.existsByUsername(request.username) } returns true

            //when

            val exception = assertFailsWith<UsernameAlreadyExistsException> {
                authService.register(request)
            }

            assertEquals("Username ${request.username} is already taken", exception.message)

            verify(exactly = 0) { userRepository.save(any()) }
        }
    }

    @Nested
    inner class Login {
        @Test
        fun `Login muvaffaqiyatli otishi`(){
            //Given
            val request = LoginRequest(
                username = "ali",
                password = "parol123",
            )

            val user = User(
                id = 1L,
                username = "ali",
                passwordHash = "hashed",
                fullName = "Ali Valiyev",
            )

            every { authenticationManager.authenticate(any()) } returns mockk()
            every { userRepository.findByUsername(request.username) } returns user

            //When
            val response = authService.login(request)

            //Then
            assertEquals("access token", response.accessToken)
            assertEquals("refresh token", response.refreshToken)
            verify { authenticationManager.authenticate(any()) }
        }

        @Test
        fun `Parol notogri`() {
            //Given
            val request = LoginRequest(
                username = "ali",
                password = "hashed",
            )

            every { authenticationManager.authenticate(any()) } throws BadCredentialsException("username yoki parol notogri")

            //When
            val exception = assertFailsWith(InvalidCredentialsException::class) {
                authService.login(request)
            }

            //Then
            assertEquals("Email yoki parol noto'g'ri", exception.message)
            verify { authenticationManager.authenticate(any()) }
            verify(exactly = 0) { userRepository.findByUsername("ali") }
        }
    }

    @Nested
    inner class Refresh {

        @BeforeEach
        fun setUp() {
            every { jwtUtil.isTokenValid(any(), any()) } returns true
            every { jwtUtil.extractUsername(any()) } returns "ali"
        }

        @Test
        fun `Token yaroqsiz`() {
            //Given
            val request = RefreshRequest(
                refreshToken = "refresh token",
            )
            every { jwtUtil.isTokenValid(any(), any()) } returns false

            //When
            val exception = assertFailsWith<InvalidTokenException> {
                authService.refresh(request)
            }

            assertEquals("Refresh token yaroqsiz", exception.message)
            verify(exactly = 0) { jwtUtil.extractUsername(any()) }
        }

        @Test
        fun `User topilmadi`() {
            val request = RefreshRequest(
                refreshToken = "refresh token",
            )

            every { userRepository.findByUsername("ali") } returns null

            val exception = assertFailsWith<InvalidTokenException> {
                authService.refresh(request)
            }

            assertEquals("Foydalanuvchi topilmadi", exception.message)
            verify { userRepository.findByUsername("ali") }
            verify(exactly = 0) { jwtUtil.generateAccessToken(any(), any()) }
        }

        @Test
        fun `Muvvaffaqiyatli`(){
            val request = RefreshRequest(
                refreshToken = "refresh token",
            )

            val user = User(
                id = 1L,
                username = "ali",
                fullName = "Ali Valiyev",
                passwordHash = "hashed"
            )
            every { userRepository.findByUsername("ali") } returns user

            //When
            val response = authService.refresh(request)

            verifyOrder {
                jwtUtil.isTokenValid(any(), any())
                jwtUtil.extractUsername(any())
                userRepository.findByUsername(any())
                jwtUtil.generateAccessToken(any(), any())
                jwtUtil.generateRefreshToken(any())
            }

            assertEquals("access token", response.accessToken)
            assertEquals("refresh token", response.refreshToken)
        }
    }

    @Nested
    inner class GetCurrentUser {
        @Test
        fun `Muvaffaqiyatli`(){
            val user = User(
                id = 1L,
                username = "ali",
                fullName = "Ali Valiyev",
                passwordHash = "hashed"
            )

            every { userRepository.findByUsername("ali") } returns user

            val response = authService.getCurrentUser(user.username)

            assertEquals(user.username, response.username)
            assertEquals(user.fullName, response.fullName)
            verify { userRepository.findByUsername("ali") }
        }

        @Test
        fun `Topilmadi`() {

            every { userRepository.findByUsername("ali") } returns null

            val exception = assertFailsWith<InvalidCredentialsException> {
                authService.getCurrentUser("ali")
            }

            verify { userRepository.findByUsername("ali") }

            assertEquals("Foydalanuvchi topilmadi", exception.message)
        }
    }
}