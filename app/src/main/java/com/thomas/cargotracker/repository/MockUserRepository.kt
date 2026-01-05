package com.thomas.cargotracker.repository

import com.thomas.cargotracker.dto.UserResponse
import com.thomas.cargotracker.keystore.TokenManager
import com.thomas.cargotracker.network.Result
import kotlinx.coroutines.delay
import javax.inject.Inject

class MockUserRepository @Inject constructor(
    private val mockUserStorage: MockUserStorage,
    private val tokenManager: TokenManager
) {
    suspend fun getCurrentUser(): Result<UserResponse> {
        delay(300)
        if (mockUserStorage.currentLoggedInEmail == null) {
            mockUserStorage.currentLoggedInEmail = tokenManager.getMockEmail()
        }
        val user = mockUserStorage.getCurrentUser()
            ?: return Result.Error("Not logged in", 401)
        return Result.Success(user.toUserResponse())
    }

    suspend fun getUserById(id: String): Result<UserResponse> {
        delay(200)
        val user = mockUserStorage.getUserById(id)
            ?: return Result.Error("User not found", 404)
        return Result.Success(user.toUserResponse())
    }

    suspend fun updateProfile(
        fullName: String? = null,
        phoneNumber: String? = null,
        address: String? = null
    ): Result<UserResponse> {
        delay(500)
        val email = mockUserStorage.currentLoggedInEmail
            ?: return Result.Error("Not logged in", 401)

        mockUserStorage.updateUser(email) { user ->
            user.copy(
                fullName = fullName ?: user.fullName,
                phoneNumber = phoneNumber ?: user.phoneNumber,
                address = address ?: user.address
            )
        }

        val updatedUser = mockUserStorage.getCurrentUser()
            ?: return Result.Error("User not found", 404)
        return Result.Success(updatedUser.toUserResponse())
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        delay(500)
        val email = mockUserStorage.currentLoggedInEmail
            ?: return Result.Error("Not logged in", 401)

        val user = mockUserStorage.getUserByEmail(email)
            ?: return Result.Error("User not found", 404)

        if (user.password != currentPassword) {
            return Result.Error("Current password is incorrect", 400)
        }

        if (newPassword != confirmPassword) {
            return Result.Error("Passwords do not match", 400)
        }

        mockUserStorage.updateUser(email) { it.copy(password = newPassword) }
        return Result.Success("Password changed successfully")
    }
}
