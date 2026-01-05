package com.thomas.cargotracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.thomas.cargotracker.ui.screens.auth.ForgotPasswordScreen
import com.thomas.cargotracker.ui.screens.auth.LoginScreen
import com.thomas.cargotracker.ui.screens.auth.RegisterScreen
import com.thomas.cargotracker.ui.screens.auth.SplashScreen
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed class AuthRoute : NavKey {
    @Serializable
    data object Splash : AuthRoute()
    @Serializable
    data object Login : AuthRoute()
    @Serializable
    data object Register : AuthRoute()
    @Serializable
    data object ForgotPassword : AuthRoute()
}

@Composable
fun AuthNavigation(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val backStack = rememberNavBackStack(AuthRoute.Splash)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = entryProvider {
            entry<AuthRoute.Splash> {
                SplashScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        backStack.clear()
                        backStack.add(AuthRoute.Login)
                    },
                    onNavigateToHome = onAuthSuccess
                )
            }

            entry<AuthRoute.Login> {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = {
                        backStack.add(AuthRoute.Register)
                    },
                    onNavigateToForgotPassword = {
                        backStack.add(AuthRoute.ForgotPassword)
                    },
                    onNavigateToHome = onAuthSuccess
                )
            }

            entry<AuthRoute.Register> {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToHome = onAuthSuccess
                )
            }

            entry<AuthRoute.ForgotPassword> {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}
