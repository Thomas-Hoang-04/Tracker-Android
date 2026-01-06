package com.thomas.cargotracker.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.ui.viewmodel.AdminViewModel
import com.thomas.cargotracker.ui.components.SecureTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateUserScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.createUserState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetCreateUserState()
    }

    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()

    // Sync TextState with ViewModel
    LaunchedEffect(passwordState.text) {
        viewModel.updateCreateUserField(password = passwordState.text.toString())
    }
    LaunchedEffect(confirmPasswordState.text) {
        viewModel.updateCreateUserField(confirmPassword = confirmPasswordState.text.toString())
    }
    
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create User") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Role Selection
            Text("Select Role", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(UserRole.PROVIDER, UserRole.SHIPPER, UserRole.CUSTOMER).forEach { role ->
                    FilterChip(
                        selected = state.role == role,
                        onClick = { viewModel.updateCreateUserField(role = role) },
                        label = { Text(role.name) }
                    )
                }
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.updateCreateUserField(email = it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            SecureTextField(
                state = passwordState,
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )

            SecureTextField(
                state = confirmPasswordState,
                label = "Confirm Password",
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Next
            )

            OutlinedTextField(
                value = state.fullName,
                onValueChange = { viewModel.updateCreateUserField(fullName = it) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = { viewModel.updateCreateUserField(phoneNumber = it) },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
            )

            Button(
                onClick = { viewModel.createUser() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Create User")
                }
            }
        }
    }
}
