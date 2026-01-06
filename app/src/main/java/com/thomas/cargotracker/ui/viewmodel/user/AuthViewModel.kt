package com.thomas.cargotracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.network.Result
import com.thomas.cargotracker.repository.AuthRepository
import com.thomas.cargotracker.repository.UserRepository
import com.thomas.cargotracker.ui.state.AuthState
import com.thomas.cargotracker.ui.state.ForgotPasswordState
import com.thomas.cargotracker.ui.state.LoginState
import com.thomas.cargotracker.ui.state.RegisterState
import com.thomas.cargotracker.ui.state.ResetPasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterState())
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordState())
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow(ResetPasswordState())
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            val isLoggedIn = authRepository.isLoggedIn()
            if (isLoggedIn) {
                loadCurrentUser()
            } else {
                _authState.update { it.copy(isLoading = false, isLoggedIn = false) }
            }
        }
    }

    private suspend fun loadCurrentUser() {
        val result = userRepository.getCurrentUser()
        when (result) {
            is Result.Success -> {
                _authState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = result.data,
                        error = null
                    )
                }
            }
            is Result.Error -> {
                _authState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        error = result.message
                    )
                }
            }
            is Result.Loading -> {}
        }
    }

    // Login
    fun updateLoginEmail(email: String) {
        _loginState.update { it.copy(email = email, error = null) }
    }

    fun updateLoginPassword(password: String) {
        _loginState.update { it.copy(password = password, error = null) }
    }

    fun login() {
        val state = _loginState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _loginState.update { it.copy(error = "Email and password are required") }
            return
        }

        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(state.email, state.password)
            when (result) {
                is Result.Success -> {
                    _loginState.update { it.copy(isLoading = false, isSuccess = true) }
                    _authState.update {
                        it.copy(
                            isLoggedIn = true,
                            currentUser = result.data.user,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _loginState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState()
    }

    // Register
    fun updateRegisterUsername(username: String) {
        _registerState.update { it.copy(username = username, error = null) }
    }

    fun updateRegisterEmail(email: String) {
        _registerState.update { it.copy(email = email, error = null) }
    }

    fun updateRegisterPassword(password: String) {
        _registerState.update { it.copy(password = password, error = null) }
    }

    fun updateRegisterConfirmPassword(confirmPassword: String) {
        _registerState.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    fun updateRegisterFullName(fullName: String) {
        _registerState.update { it.copy(fullName = fullName, error = null) }
    }

    fun updateRegisterPhoneNumber(phoneNumber: String) {
        _registerState.update { it.copy(phoneNumber = phoneNumber, error = null) }
    }

    fun updateRegisterAddress(address: String) {
        _registerState.update { it.copy(address = address, error = null) }
    }

    fun register() {
        val state = _registerState.value
        if (state.username.isBlank() || state.email.isBlank() ||
            state.password.isBlank() || state.fullName.isBlank()) {
            _registerState.update { it.copy(error = "Please fill in all required fields") }
            return
        }

        if (state.password != state.confirmPassword) {
            _registerState.update { it.copy(error = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.register(
                username = state.username,
                email = state.email,
                password = state.password,
                confirmPassword = state.confirmPassword,
                fullName = state.fullName,
                phoneNumber = state.phoneNumber.ifBlank { null },
                address = state.address.ifBlank { null }
            )
            when (result) {
                is Result.Success -> {
                    _registerState.update { it.copy(isLoading = false, isSuccess = true) }
                    _authState.update {
                        it.copy(
                            isLoggedIn = true,
                            currentUser = result.data.user,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _registerState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState()
    }

    // Forgot Password
    fun updateForgotPasswordEmail(email: String) {
        _forgotPasswordState.update { it.copy(email = email, error = null) }
    }

    fun forgotPassword() {
        val state = _forgotPasswordState.value
        if (state.email.isBlank()) {
            _forgotPasswordState.update { it.copy(error = "Email is required") }
            return
        }

        viewModelScope.launch {
            _forgotPasswordState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.forgotPassword(state.email)
            when (result) {
                is Result.Success -> {
                    _forgotPasswordState.update {
                        it.copy(isLoading = false, isSuccess = true, message = result.data)
                    }
                }
                is Result.Error -> {
                    _forgotPasswordState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }

    // Reset Password
    fun updateResetPasswordToken(token: String) {
        _resetPasswordState.update { it.copy(token = token, error = null) }
    }

    fun updateResetPasswordNewPassword(password: String) {
        _resetPasswordState.update { it.copy(newPassword = password, error = null) }
    }

    fun updateResetPasswordConfirmPassword(confirmPassword: String) {
        _resetPasswordState.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    fun resetPassword() {
        val state = _resetPasswordState.value
        if (state.token.isBlank() || state.newPassword.isBlank()) {
            _resetPasswordState.update { it.copy(error = "All fields are required") }
            return
        }

        if (state.newPassword != state.confirmPassword) {
            _resetPasswordState.update { it.copy(error = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _resetPasswordState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.resetPassword(
                token = state.token,
                newPassword = state.newPassword,
                confirmPassword = state.confirmPassword
            )
            when (result) {
                is Result.Success -> {
                    _resetPasswordState.update {
                        it.copy(isLoading = false, isSuccess = true, message = result.data)
                    }
                }
                is Result.Error -> {
                    _resetPasswordState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = ResetPasswordState()
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            authRepository.logout()
            _authState.value = AuthState(isLoading = false, isLoggedIn = false)
            resetLoginState()
            resetRegisterState()
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }
}
