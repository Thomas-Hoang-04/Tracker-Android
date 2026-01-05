package com.thomas.cargotracker.repository

import com.thomas.cargotracker.dto.AuthResponse
import com.thomas.cargotracker.dto.TokenResponse
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.keystore.TokenManager
import com.thomas.cargotracker.network.Result
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class MockAuthRepository @Inject constructor(
    private val tokenManager: TokenManager,
    private val mockUserStorage: MockUserStorage
) {
    suspend fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String,
        phoneNumber: String? = null,
        address: String? = null
    ): Result<AuthResponse> {
        delay(1000)

        if (password != confirmPassword) {
            return Result.Error("Passwords do not match", 400)
        }

        if (mockUserStorage.registeredUsers.containsKey(email)) {
            return Result.Error("Email already registered", 409)
        }

        val newUser = MockUserStorage.MockUser(
            id = "mock-user-${System.currentTimeMillis()}",
            username = username,
            email = email,
            password = password,
            fullName = fullName,
            phoneNumber = phoneNumber,
            role = UserRole.CUSTOMER,
            address = address
        )
        mockUserStorage.addUser(newUser)
        mockUserStorage.currentLoggedInEmail = email
        tokenManager.saveMockEmail(email)

        val authResponse = createAuthResponse(newUser)
        saveTokens(authResponse)
        return Result.Success(authResponse)
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        delay(800)

        val user = mockUserStorage.getUserByEmail(email)
            ?: return Result.Error("Invalid email or password", 401)

        if (user.password != password) {
            return Result.Error("Invalid email or password", 401)
        }

        mockUserStorage.currentLoggedInEmail = email
        tokenManager.saveMockEmail(email)
        val authResponse = createAuthResponse(user)
        saveTokens(authResponse)
        return Result.Success(authResponse)
    }

    suspend fun refreshToken(): Result<TokenResponse> {
        delay(300)

        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.Error("No refresh token available", 401)

        val expiresAt = Instant.now().plus(1, ChronoUnit.HOURS)
        val tokenResponse = TokenResponse(
            accessToken = "mock_access_${System.currentTimeMillis()}",
            refreshToken = "mock_refresh_${System.currentTimeMillis()}",
            tokenType = "Bearer",
            expiresIn = 3600,
            expiresAt = expiresAt.toString()
        )

        tokenManager.saveAccessToken(tokenResponse.accessToken)
        tokenResponse.refreshToken?.let { tokenManager.saveRefreshToken(it) }

        return Result.Success(tokenResponse)
    }

    suspend fun logout(): Result<Unit> {
        delay(200)
        mockUserStorage.currentLoggedInEmail = null
        tokenManager.clearMockEmail()
        tokenManager.clearTokens()
        return Result.Success(Unit)
    }

    suspend fun forgotPassword(email: String): Result<String> {
        delay(500)

        return if (mockUserStorage.registeredUsers.containsKey(email)) {
            Result.Success("Password reset email sent to $email")
        } else {
            Result.Error("Email not found", 404)
        }
    }

    suspend fun resetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        delay(500)

        if (newPassword != confirmPassword) {
            return Result.Error("Passwords do not match", 400)
        }

        return Result.Success("Password reset successfully")
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }

    suspend fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    private suspend fun saveTokens(authResponse: AuthResponse) {
        tokenManager.saveTokens(
            accessToken = authResponse.accessToken,
            refreshToken = authResponse.refreshToken
        )
    }

    private fun createAuthResponse(user: MockUserStorage.MockUser): AuthResponse {
        val expiresAt = Instant.now().plus(1, ChronoUnit.HOURS)
        return AuthResponse(
            user = user.toUserResponse(),
            accessToken = "mock_access_${System.currentTimeMillis()}",
            refreshToken = "mock_refresh_${System.currentTimeMillis()}",
            tokenType = "Bearer",
            expiresIn = 3600,
            expiresAt = expiresAt.toString()
        )
    }
}

