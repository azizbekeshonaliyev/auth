package uz.mkb.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank("Ism kiritilishi kerak")
    val fullName: String,

    @field:NotBlank("Username kiritilishi kerak")
    val username: String,

    @field:Size(min = 6, message = "Parol kamida 6 belgidan iborat bo'lishi kerak")
    @field:NotBlank("Parol kiritilishi kerak")
    val password: String
)

data class LoginRequest(
    @field:NotBlank("Username kiritilishi kerak")
    val username: String,

    @field:NotBlank("Parol kiritilishi kerak")
    val password: String
)

data class RefreshRequest(
    @field:NotBlank("refresh token kiritilishi kerak")
    val refreshToken: String
)

data class ChangePasswordRequest(
    @field:NotBlank("Eski parol kiritilishi kerak")
    val oldPassword: String,

    @field:Size(min = 6, message = "Yangi parol kamida 6 belgidan iborat bo'lishi kerak")
    @field:NotBlank("Yangi parol kiritilishi kerak")
    val newPassword: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer",
)

data class UserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String
)

data class ErrorResponse(
    val message: String,
    val details: List<String> = emptyList()
)