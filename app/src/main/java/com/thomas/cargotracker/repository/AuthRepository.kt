package com.thomas.cargotracker.repository

import com.google.gson.Gson
import com.thomas.cargotracker.network.Result
import com.thomas.cargotracker.dto.AuthResponse
import com.thomas.cargotracker.dto.ForgotPasswordRequest
import com.thomas.cargotracker.dto.LoginRequest
import com.thomas.cargotracker.dto.RefreshTokenRequest
import com.thomas.cargotracker.dto.RegisterRequest
import com.thomas.cargotracker.dto.ResetPasswordRequest
import com.thomas.cargotracker.dto.TokenResponse
import com.thomas.cargotracker.keystore.TokenManager
import com.thomas.cargotracker.network.AuthInterface

class AuthRepository(
    private val authInterface: AuthInterface,
    private val tokenManager: TokenManager,
    gson: Gson
): BaseRepository(gson) {
    suspend fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String,
        phoneNumber: String? = null,
        address: String? = null
    ): Result<AuthResponse> {
        return safeApiCall {
            authInterface.register(
                RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    address = address
                )
            )
        }.also { result ->
            if (result is Result.Success) {
                saveTokens(result.data)
            }
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return safeApiCall {
            authInterface.login(LoginRequest(email, password))
        }.also { result ->
            if (result is Result.Success) {
                saveTokens(result.data)
            }
        }
    }

    suspend fun refreshToken(): Result<TokenResponse> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.Error("No refresh token available", 401)

        return safeApiCall {
            authInterface.refreshToken(RefreshTokenRequest(refreshToken))
        }.also { result ->
            if (result is Result.Success) {
                tokenManager.saveAccessToken(result.data.accessToken)
                result.data.refreshToken?.let { tokenManager.saveRefreshToken(it) }
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        val refreshToken = tokenManager.getRefreshToken()

        return if (refreshToken != null) {
            val result = safeApiCall {
                authInterface.logout(RefreshTokenRequest(refreshToken))
            }
            tokenManager.clearTokens()
            result.map { }
        } else {
            tokenManager.clearTokens()
            Result.Success(Unit)
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return safeApiCall {
            authInterface.forgotPassword(ForgotPasswordRequest(email))
        }.map { it.message }
    }

    suspend fun resetPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        return safeApiCall {
            authInterface.resetPassword(
                ResetPasswordRequest(
                    token = token,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            )
        }.map { it.message }
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
}
