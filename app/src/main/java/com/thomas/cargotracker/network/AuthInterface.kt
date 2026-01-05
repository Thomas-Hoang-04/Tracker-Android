package com.thomas.cargotracker.network

import com.thomas.cargotracker.dto.AuthResponse
import com.thomas.cargotracker.dto.ForgotPasswordRequest
import com.thomas.cargotracker.dto.LoginRequest
import com.thomas.cargotracker.dto.RefreshTokenRequest
import com.thomas.cargotracker.dto.RegisterRequest
import com.thomas.cargotracker.dto.ResetPasswordRequest
import com.thomas.cargotracker.dto.SuccessResponse
import com.thomas.cargotracker.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthInterface {
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<TokenResponse>

    @POST("api/auth/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequest
    ): Response<SuccessResponse>

    @POST("api/auth/forgotPassword")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<SuccessResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<SuccessResponse>
}