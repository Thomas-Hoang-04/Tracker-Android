package com.thomas.cargotracker.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thomas.cargotracker.ui.components.AuthTextField
import com.thomas.cargotracker.ui.components.PasswordTextField
import com.thomas.cargotracker.ui.components.PrimaryButton
import com.thomas.cargotracker.ui.components.SecondaryButton
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val registerState by authViewModel.registerState.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(registerState.isSuccess) {
        if (registerState.isSuccess) {
            authViewModel.resetRegisterState()
            onNavigateToHome()
        }
    }

    LaunchedEffect(registerState.error) {
        registerState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Logo",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = "Fill in your details to get started",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Register Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Username
                        AuthTextField(
                            value = registerState.username,
                            onValueChange = { authViewModel.updateRegisterUsername(it) },
                            label = "Username",
                            leadingIcon = Icons.Default.Person,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                            enabled = !registerState.isLoading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Full Name
                        AuthTextField(
                            value = registerState.fullName,
                            onValueChange = { authViewModel.updateRegisterFullName(it) },
                            label = "Full Name",
                            leadingIcon = Icons.Default.Person,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                            enabled = !registerState.isLoading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email
                        AuthTextField(
                            value = registerState.email,
                            onValueChange = { authViewModel.updateRegisterEmail(it) },
                            label = "Email",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                            enabled = !registerState.isLoading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone Number (Optional)
                        AuthTextField(
                            value = registerState.phoneNumber,
                            onValueChange = { authViewModel.updateRegisterPhoneNumber(it) },
                            label = "Phone Number (Optional)",
                            leadingIcon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                            enabled = !registerState.isLoading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Address (Optional)
                        AuthTextField(
                            value = registerState.address,
                            onValueChange = { authViewModel.updateRegisterAddress(it) },
                            label = "Address (Optional)",
                            leadingIcon = Icons.Default.Home,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                            enabled = !registerState.isLoading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password
                        PasswordTextField(
                            value = registerState.password,
                            onValueChange = { authViewModel.updateRegisterPassword(it) },
                            label = "Password",
                            leadingIcon = Icons.Default.Lock,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                            enabled = !registerState.isLoading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Confirm Password
                        PasswordTextField(
                            value = registerState.confirmPassword,
                            onValueChange = { authViewModel.updateRegisterConfirmPassword(it) },
                            label = "Confirm Password",
                            leadingIcon = Icons.Default.Lock,
                            imeAction = ImeAction.Done,
                            onImeAction = {
                                focusManager.clearFocus()
                                authViewModel.register()
                            },
                            enabled = !registerState.isLoading
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        PrimaryButton(
                            text = "Create Account",
                            onClick = {
                                focusManager.clearFocus()
                                authViewModel.register()
                            },
                            isLoading = registerState.isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Already have an account?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            SecondaryButton(
                                text = "Sign In",
                                onClick = onNavigateBack,
                                enabled = !registerState.isLoading
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

