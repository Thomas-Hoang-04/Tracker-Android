package com.thomas.cargotracker.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val fullName: String,
    val phoneNumber: String? = null,
    val address: String? = null
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)

data class UpdateProfileRequest(
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val role: UserRole,
    val address: String?,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)

data class AuthResponse(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val expiresAt: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val expiresAt: String
)
