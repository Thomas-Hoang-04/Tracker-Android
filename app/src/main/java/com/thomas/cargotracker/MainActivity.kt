package com.thomas.cargotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thomas.cargotracker.di.AppContainer
import com.thomas.cargotracker.ui.navigation.AuthNavigation
import com.thomas.cargotracker.ui.screens.main.MainScreen
import com.thomas.cargotracker.ui.theme.CargoTrackerTheme
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy {
        (application as CargoTrackerApplication).appContainer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CargoTrackerTheme {
                CargoTrackerApp(appContainer = appContainer)
            }
        }
    }
}

@Composable
fun CargoTrackerApp(appContainer: AppContainer) {
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }

    val authViewModel: AuthViewModel = viewModel(
        factory = appContainer.authViewModelFactory
    )

    if (isAuthenticated) {
        MainScreen(
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
