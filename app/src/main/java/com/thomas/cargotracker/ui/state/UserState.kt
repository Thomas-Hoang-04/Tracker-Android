package com.thomas.cargotracker.ui.state

import com.thomas.cargotracker.dto.UserResponse

data class UserProfileState(
    val user: UserResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class UpdateProfileState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

