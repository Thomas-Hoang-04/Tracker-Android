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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import com.thomas.cargotracker.ui.components.AuthTextField
import com.thomas.cargotracker.ui.components.SecureTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.ExperimentalFoundationApi
import com.thomas.cargotracker.ui.components.PrimaryButton
import com.thomas.cargotracker.ui.components.SecondaryButton
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val registerState by authViewModel.registerState.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Password States
    val passwordState = rememberTextFieldState(registerState.password)
    val confirmPasswordState = rememberTextFieldState(registerState.confirmPassword)

    // Sync ViewModel reset/update to TextFieldState
    LaunchedEffect(registerState.password) {
        if (passwordState.text.toString() != registerState.password) {
            passwordState.setTextAndPlaceCursorAtEnd(registerState.password)
        }
    }
    LaunchedEffect(registerState.confirmPassword) {
        if (confirmPasswordState.text.toString() != registerState.confirmPassword) {
            confirmPasswordState.setTextAndPlaceCursorAtEnd(registerState.confirmPassword)
        }
    }

    // Sync TextFieldState to ViewModel
    LaunchedEffect(passwordState) {
        snapshotFlow { passwordState.text }.collectLatest {
            if (it.toString() != registerState.password) authViewModel.updateRegisterPassword(it.toString())
        }
    }
    LaunchedEffect(confirmPasswordState) {
        snapshotFlow { confirmPasswordState.text }.collectLatest {
            if (it.toString() != registerState.confirmPassword) authViewModel.updateRegisterConfirmPassword(it.toString())
        }
    }

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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Logo",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Fill remaining space
                    shape = RoundedCornerShape(
                        topStart = 32.dp,
                        topEnd = 32.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Scrollable Input Area
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AuthTextField(
                                value = registerState.username,
                                onValueChange = { authViewModel.updateRegisterUsername(it) },
                                label = "Username",
                                leadingIcon = Icons.Default.Person,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                                enabled = !registerState.isLoading
                            )

                            AuthTextField(
                                value = registerState.fullName,
                                onValueChange = { authViewModel.updateRegisterFullName(it) },
                                label = "Full Name",
                                leadingIcon = Icons.Default.Person,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                                enabled = !registerState.isLoading
                            )

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

                            AuthTextField(
                                value = registerState.address,
                                onValueChange = { authViewModel.updateRegisterAddress(it) },
                                label = "Address (Optional)",
                                leadingIcon = Icons.Default.Home,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                                enabled = !registerState.isLoading
                            )

                            SecureTextField(
                                state = passwordState,
                                label = "Password",
                                leadingIcon = Icons.Default.Lock,
                                imeAction = ImeAction.Next,
                            )

                            SecureTextField(
                                state = confirmPasswordState,
                                label = "Confirm Password",
                                leadingIcon = Icons.Default.Lock,
                                imeAction = ImeAction.Done,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fixed Bottom Section
                        Column {
                            PrimaryButton(
                                text = "Create Account",
                                onClick = {
                                    focusManager.clearFocus()
                                    authViewModel.register()
                                },
                                isLoading = registerState.isLoading
                            )

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
                        }
                    }
                }
            }
        }
    }
}


