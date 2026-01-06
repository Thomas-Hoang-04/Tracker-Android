package com.thomas.cargotracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.dto.AdminCreateUserRequest
import com.thomas.cargotracker.dto.UserResponse
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.network.Result
import com.thomas.cargotracker.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUserListState(
    val users: List<UserResponse> = emptyList(),
    val filteredUsers: List<UserResponse> = emptyList(),
    val selectedRoleFilter: UserRole? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CreateUserState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullName: String = "",
    val role: UserRole = UserRole.PROVIDER,
    val phoneNumber: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userListState = MutableStateFlow(AdminUserListState())
    val userListState: StateFlow<AdminUserListState> = _userListState.asStateFlow()

    private val _createUserState = MutableStateFlow(CreateUserState())
    val createUserState: StateFlow<CreateUserState> = _createUserState.asStateFlow()

    // --- User Management ---

    fun loadAllUsers() {
        viewModelScope.launch {
            _userListState.update { it.copy(isLoading = true, error = null) }
            when (val result = userRepository.getAllUsers()) {
                is Result.Success -> {
                    _userListState.update { 
                        it.copy(
                            isLoading = false, 
                            users = result.data,
                            filteredUsers = filterUsers(result.data, it.selectedRoleFilter)
                        )
                    }
                }
                is Result.Error -> {
                    _userListState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun filterUsersByRole(role: UserRole?) {
        _userListState.update { 
            it.copy(
                selectedRoleFilter = role,
                filteredUsers = filterUsers(it.users, role)
            )
        }
    }

    private fun filterUsers(users: List<UserResponse>, role: UserRole?): List<UserResponse> {
        return if (role == null) users else users.filter { it.role == role }
    }

    fun toggleUserActivation(userId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            // Optimistic update or just show loading? Let's show loading/refresh
            // For now, simpler to just redo the call or handle single item update
            // We'll call the API then refresh list
            val result = if (currentStatus) {
                userRepository.deactivateUser(userId)
            } else {
                userRepository.activateUser(userId)
            }
            
            when (result) {
                is Result.Success -> loadAllUsers() // Refresh list
                is Result.Error -> {
                     _userListState.update { it.copy(error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    // --- Create User ---

    fun updateCreateUserField(
        email: String? = null,
        password: String? = null,
        confirmPassword: String? = null,
        fullName: String? = null,
        role: UserRole? = null,
        phoneNumber: String? = null,
        address: String? = null
    ) {
        _createUserState.update {
            it.copy(
                email = email ?: it.email,
                password = password ?: it.password,
                confirmPassword = confirmPassword ?: it.confirmPassword,
                fullName = fullName ?: it.fullName,
                role = role ?: it.role,
                phoneNumber = phoneNumber ?: it.phoneNumber,
                address = address ?: it.address,
                error = null
            )
        }
    }

    fun createUser() {
        val state = _createUserState.value
        
        if (state.password != state.confirmPassword) {
            _createUserState.update { it.copy(error = "Passwords do not match") }
            return
        }
        
        if (state.email.isBlank() || state.password.isBlank() || state.fullName.isBlank()) {
             _createUserState.update { it.copy(error = "Required fields are missing") }
             return
        }

        viewModelScope.launch {
            _createUserState.update { it.copy(isLoading = true, error = null) }
            val request = AdminCreateUserRequest(
                email = state.email,
                password = state.password,
                confirmPassword = state.confirmPassword,
                fullName = state.fullName,
                role = state.role,
                phoneNumber = state.phoneNumber.ifBlank { null },
                address = state.address.ifBlank { null }
            )
            
            when (val result = userRepository.createUserByAdmin(request)) {
                is Result.Success -> {
                    _createUserState.update { it.copy(isLoading = false, isSuccess = true) }
                    loadAllUsers() // Refresh list if needed when returning
                }
                is Result.Error -> {
                    _createUserState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun resetCreateUserState() {
        _createUserState.value = CreateUserState()
    }
    
    fun clearError() {
         _userListState.update { it.copy(error = null) }
    }
}
