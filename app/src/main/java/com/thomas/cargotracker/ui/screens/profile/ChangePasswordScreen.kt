package com.thomas.cargotracker.ui.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.ui.components.SecureTextField
import com.thomas.cargotracker.ui.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val state by viewModel.changePasswordState.collectAsState()

    val currentPasswordState = rememberTextFieldState(state.currentPassword)
    val newPasswordState = rememberTextFieldState(state.newPassword)
    val confirmPasswordState = rememberTextFieldState(state.confirmPassword)

    LaunchedEffect(Unit) {
        viewModel.resetChangePasswordState()
    }

    // Sync ViewModel reset to TextFieldState
    LaunchedEffect(state.currentPassword) {
        if (currentPasswordState.text.toString() != state.currentPassword) {
            currentPasswordState.setTextAndPlaceCursorAtEnd(state.currentPassword)
        }
    }
    LaunchedEffect(state.newPassword) {
        if (newPasswordState.text.toString() != state.newPassword) {
            newPasswordState.setTextAndPlaceCursorAtEnd(state.newPassword)
        }
    }
    LaunchedEffect(state.confirmPassword) {
        if (confirmPasswordState.text.toString() != state.confirmPassword) {
            confirmPasswordState.setTextAndPlaceCursorAtEnd(state.confirmPassword)
        }
    }

    // Sync TextFieldState to ViewModel
    LaunchedEffect(currentPasswordState) {
        snapshotFlow { currentPasswordState.text }.collectLatest {
            if (it.toString() != state.currentPassword) viewModel.updateCurrentPassword(it.toString())
        }
    }
    LaunchedEffect(newPasswordState) {
        snapshotFlow { newPasswordState.text }.collectLatest {
            if (it.toString() != state.newPassword) viewModel.updateNewPassword(it.toString())
        }
    }
    LaunchedEffect(confirmPasswordState) {
        snapshotFlow { confirmPasswordState.text }.collectLatest {
            if (it.toString() != state.confirmPassword) viewModel.updateConfirmPassword(it.toString())
        }
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (state.isSuccess) {
                Text(
                    text = state.message ?: "Password changed successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SecureTextField(
                state = currentPasswordState,
                label = "Current Password",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )

            SecureTextField(
                state = newPasswordState,
                label = "New Password",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )

            SecureTextField(
                state = confirmPasswordState,
                label = "Confirm New Password",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Done
            )

            Button(
                onClick = { viewModel.changePassword() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Change Password")
                }
            }
        }
    }
}
