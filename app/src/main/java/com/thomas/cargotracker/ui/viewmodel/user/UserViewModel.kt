package com.thomas.cargotracker.ui.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.network.Result
import com.thomas.cargotracker.repository.UserRepository
import com.thomas.cargotracker.ui.state.ChangePasswordState
import com.thomas.cargotracker.ui.state.UpdateProfileState
import com.thomas.cargotracker.ui.state.UserProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(UserProfileState())
    val profileState: StateFlow<UserProfileState> = _profileState.asStateFlow()

    private val _updateProfileState = MutableStateFlow(UpdateProfileState())
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState.asStateFlow()

    private val _changePasswordState = MutableStateFlow(ChangePasswordState())
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    fun loadCurrentUser() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, error = null) }
            when (val result = userRepository.getCurrentUser()) {
                is Result.Success -> {
                    _profileState.update {
                        it.copy(isLoading = false, user = result.data, error = null)
                    }
                    // Pre-fill update profile form
                    _updateProfileState.update {
                        it.copy(
                            fullName = result.data.fullName,
                            phoneNumber = result.data.phoneNumber ?: "",
                            address = result.data.address ?: ""
                        )
                    }
                }
                is Result.Error -> {
                    _profileState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun loadUserById(id: String) {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, error = null) }
            when (val result = userRepository.getUserById(id)) {
                is Result.Success -> {
                    _profileState.update {
                        it.copy(isLoading = false, user = result.data, error = null)
                    }
                }
                is Result.Error -> {
                    _profileState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    // Update Profile
    fun updateFullName(fullName: String) {
        _updateProfileState.update { it.copy(fullName = fullName, error = null) }
    }

    fun updatePhoneNumber(phoneNumber: String) {
        _updateProfileState.update { it.copy(phoneNumber = phoneNumber, error = null) }
    }

    fun updateAddress(address: String) {
        _updateProfileState.update { it.copy(address = address, error = null) }
    }

    fun updateProfile() {
        val state = _updateProfileState.value
        viewModelScope.launch {
            _updateProfileState.update { it.copy(isLoading = true, error = null) }
            when (val result = userRepository.updateProfile(
                fullName = state.fullName.ifBlank { null },
                phoneNumber = state.phoneNumber.ifBlank { null },
                address = state.address.ifBlank { null }
            )) {
                is Result.Success -> {
                    _updateProfileState.update { it.copy(isLoading = false, isSuccess = true) }
                    _profileState.update { it.copy(user = result.data) }
                }
                is Result.Error -> {
                    _updateProfileState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetUpdateProfileState() {
        val user = _profileState.value.user
        _updateProfileState.value = UpdateProfileState(
            fullName = user?.fullName ?: "",
            phoneNumber = user?.phoneNumber ?: "",
            address = user?.address ?: ""
        )
    }

    // Change Password
    fun updateCurrentPassword(password: String) {
        _changePasswordState.update { it.copy(currentPassword = password, error = null) }
    }

    fun updateNewPassword(password: String) {
        _changePasswordState.update { it.copy(newPassword = password, error = null) }
    }

    fun updateConfirmPassword(password: String) {
        _changePasswordState.update { it.copy(confirmPassword = password, error = null) }
    }

    fun changePassword() {
        val state = _changePasswordState.value
        if (state.currentPassword.isBlank() || state.newPassword.isBlank()) {
            _changePasswordState.update { it.copy(error = "All fields are required") }
            return
        }

        if (state.newPassword != state.confirmPassword) {
            _changePasswordState.update { it.copy(error = "New passwords do not match") }
            return
        }

        if (state.currentPassword == state.newPassword) {
            _changePasswordState.update { it.copy(error = "New password must be different from current password") }
            return
        }

        viewModelScope.launch {
            _changePasswordState.update { it.copy(isLoading = true, error = null) }
            when (val result = userRepository.changePassword(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword,
                confirmPassword = state.confirmPassword
            )) {
                is Result.Success -> {
                    _changePasswordState.update {
                        it.copy(isLoading = false, isSuccess = true, message = result.data)
                    }
                }
                is Result.Error -> {
                    _changePasswordState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordState()
    }

    fun clearProfileError() {
        _profileState.update { it.copy(error = null) }
    }
}
