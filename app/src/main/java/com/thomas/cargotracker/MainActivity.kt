package com.thomas.cargotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.ui.navigation.AuthNavigation
import com.thomas.cargotracker.ui.navigation.MainNavigation
import com.thomas.cargotracker.ui.theme.CargoTrackerTheme
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CargoTrackerTheme {
                CargoTrackerApp()
            }
        }
    }
}

@Composable
fun CargoTrackerApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }

    if (isAuthenticated) {
        MainNavigation(
            authViewModel = authViewModel,
            userRole = authState.currentUser?.role ?: UserRole.PROVIDER,
            onLogout = {
                authViewModel.logout()
                isAuthenticated = false
            }
        )
    } else {
        AuthNavigation(
            authViewModel = authViewModel,
            onAuthSuccess = {
                isAuthenticated = true
            }
        )
    }
}

