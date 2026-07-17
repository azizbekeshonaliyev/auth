package uz.mkb.auth.service

import jakarta.transaction.Transactional
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import uz.mkb.auth.dto.AuthResponse
import uz.mkb.auth.dto.LoginRequest
import uz.mkb.auth.dto.RefreshRequest
import uz.mkb.auth.dto.RegisterRequest
import uz.mkb.auth.dto.UserResponse
import uz.mkb.auth.exception.InvalidCredentialsException
import uz.mkb.auth.exception.InvalidTokenException
import uz.mkb.auth.exception.UsernameAlreadyExistsException
import uz.mkb.auth.model.User
import uz.mkb.auth.repository.UserRepository
import uz.mkb.auth.security.JwtUtil

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil
) {

    @Transactional
    fun register(request : RegisterRequest) : AuthResponse {

        val username = request.username

        if (userRepository.existsByUsername(username)) {
            throw UsernameAlreadyExistsException("Username $username is already taken")
        }

        val user = User(
            username = request.username,
            passwordHash = requireNotNull(passwordEncoder.encode(request.password)) { "Password cannot be null" },
            fullName = request.fullName.trim()
        )

        userRepository.save(user)
        return buildAuthResponse(user.username, user.role.name)
    }

    fun login(request: LoginRequest): AuthResponse {
        val username = request.username.trim()
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, request.password))
        } catch (ex: BadCredentialsException) {
            throw InvalidCredentialsException("Email yoki parol noto'g'ri")
        }
        val user = userRepository.findByUsername(username) ?: throw InvalidCredentialsException("Email yoki parol noto'g'ri")

        return buildAuthResponse(user.username, user.role.name)
    }

    fun refresh(request: RefreshRequest): AuthResponse {
        if (!jwtUtil.isTokenValid(request.refreshToken, "refresh")) throw InvalidTokenException("Refresh token yaroqsiz")
        val email = jwtUtil.extractUsername(request.refreshToken)
        val user = userRepository.findByUsername(email) ?: throw InvalidTokenException("Foydalanuvchi topilmadi")
        return buildAuthResponse(user.username, user.role.name)
    }


    fun getCurrentUser(username: String): UserResponse {
        val user = userRepository.findByUsername(username) ?: throw InvalidCredentialsException("Foydalanuvchi topilmadi")
        return UserResponse(user.id ?: throw IllegalStateException("User id"), user.username, user.fullName, user.role.name)
    }


    private fun buildAuthResponse(username: String, role: String) = AuthResponse(
        accessToken = jwtUtil.generateAccessToken(username, role),
        refreshToken = jwtUtil.generateRefreshToken(username),
        expiresIn = jwtUtil.accessTokenExpirationMs / 1000
    )
}