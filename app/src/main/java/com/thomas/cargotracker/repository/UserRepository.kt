package com.thomas.cargotracker.repository

import com.google.gson.Gson
import com.thomas.cargotracker.dto.ChangePasswordRequest
import com.thomas.cargotracker.dto.UpdateProfileRequest
import com.thomas.cargotracker.dto.UserResponse
import com.thomas.cargotracker.network.ApiInterface
import com.thomas.cargotracker.network.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    gson: Gson
): BaseRepository(gson) {
    suspend fun getCurrentUser(): Result<UserResponse> {
        return safeApiCall {
            apiInterface.getCurrentUser()
        }
    }

    suspend fun getUserById(id: String): Result<UserResponse> {
        return safeApiCall {
            apiInterface.getUserById(id)
        }
    }

    suspend fun updateProfile(
        fullName: String? = null,
        phoneNumber: String? = null,
        address: String? = null
    ): Result<UserResponse> {
        return safeApiCall {
            apiInterface.updateProfile(
                UpdateProfileRequest(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    address = address
                )
            )
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<String> {
        return safeApiCall {
            apiInterface.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            )
        }.map { it.message }
    }
}
