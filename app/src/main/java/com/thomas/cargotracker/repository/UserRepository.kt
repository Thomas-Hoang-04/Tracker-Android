package com.thomas.cargotracker.repository

import com.google.gson.Gson
import com.thomas.cargotracker.dto.AdminCreateUserRequest
import com.thomas.cargotracker.dto.ChangePasswordRequest
import com.thomas.cargotracker.dto.UpdateProfileRequest
import com.thomas.cargotracker.dto.UserResponse
import com.thomas.cargotracker.dto.UserRole
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
    suspend fun getAllUsers(): Result<List<UserResponse>> {
        return safeApiCall {
            apiInterface.getAllUsers()
        }
    }

    suspend fun getUsersByRole(role: UserRole): Result<List<UserResponse>> {
        return safeApiCall {
            apiInterface.getUsersByRole(role)
        }
    }

    suspend fun createUserByAdmin(request: AdminCreateUserRequest): Result<UserResponse> {
        return safeApiCall {
            apiInterface.createUserByAdmin(request)
        }
    }

    suspend fun activateUser(id: String): Result<String> {
        return safeApiCall {
            apiInterface.activateUser(id)
        }.map { it.message }
    }

    suspend fun deactivateUser(id: String): Result<String> {
        return safeApiCall {
            apiInterface.deactivateUser(id)
        }.map { it.message }
    }
}
