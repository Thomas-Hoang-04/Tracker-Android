package com.thomas.cargotracker.ui.navigation

import MainScreen
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.ui.screens.profile.ProfileContent
import com.thomas.cargotracker.ui.screens.provider.ProviderCreateOrderScreen
import com.thomas.cargotracker.ui.screens.provider.ProviderHistoryScreen
import com.thomas.cargotracker.ui.screens.provider.ProviderHomeScreen
import com.thomas.cargotracker.ui.screens.provider.ProviderOrderDetailsScreen
import com.thomas.cargotracker.ui.screens.customer.CustomerHomeScreen
import com.thomas.cargotracker.ui.screens.customer.CustomerHistoryScreen
import com.thomas.cargotracker.ui.screens.customer.CustomerSearchUserScreen
import com.thomas.cargotracker.ui.screens.customer.CustomerOrderDetailsScreen
import com.thomas.cargotracker.ui.screens.shipper.ShipperHomeScreen
import com.thomas.cargotracker.ui.screens.shipper.ShipperFindOrderScreen
import com.thomas.cargotracker.ui.screens.shipper.ShipperHistoryScreen
import com.thomas.cargotracker.ui.screens.shipper.ShipperOrderDetailsScreen
import kotlinx.serialization.Serializable

@Serializable
sealed class MainRoute : NavKey {
    @Serializable
    data object Home : MainRoute()
    @Serializable
    data object Profile : MainRoute()

    @Serializable
    data object ProviderCreate : MainRoute()
    @Serializable
    data object ProviderHistory : MainRoute()
    @Serializable
    data class ProviderOrderDetails(val orderId: String) : MainRoute()

    // Customer
    @Serializable
    data object CustomerHome : MainRoute()
    @Serializable
    data object CustomerHistory : MainRoute()
    @Serializable
    data object CustomerSearch : MainRoute()
    @Serializable
    data class CustomerOrderDetails(val orderId: String) : MainRoute()

    // Shipper
    @Serializable
    data object ShipperHome : MainRoute()
    @Serializable
    data object ShipperFindOrder : MainRoute()
    @Serializable
    data object ShipperHistory : MainRoute()
    @Serializable
    data class ShipperOrderDetails(val orderId: String) : MainRoute()
}

@Composable
fun MainNavigation(
    userRole: UserRole,
    onLogout: () -> Unit
) {
    val backStack = rememberNavBackStack(MainRoute.Home)

    MainScreen(
        userRole = userRole,
        backStack = backStack,
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
            entryProvider = entryProvider {
                entry<MainRoute.Home> {
                    when (userRole) {
                        UserRole.PROVIDER -> ProviderHomeScreen(
                            onSeeOrderState = { /* TODO */ },
                            onCreateOrder = { backStack.add(MainRoute.ProviderCreate) },
                            onOrderHistory = { backStack.add(MainRoute.ProviderHistory) },
                            onOrderDetails = { orderId -> backStack.add(MainRoute.ProviderOrderDetails(orderId)) }
                        )
                        UserRole.CUSTOMER -> CustomerHomeScreen(
                             onOrderDetails = { orderId -> backStack.add(MainRoute.CustomerOrderDetails(orderId)) }
                        )
                        UserRole.SHIPPER -> ShipperHomeScreen(
                             onOrderDetails = { orderId -> backStack.add(MainRoute.ShipperOrderDetails(orderId)) }
                        )
                        else -> {}
                    }
                }

                entry<MainRoute.Profile> {
                    ProfileContent(userRole = userRole, onLogout = onLogout)
                }

                entry<MainRoute.ProviderCreate> {
                    ProviderCreateOrderScreen(
                        onOrderCreated = {
                            backStack.clear()
                            backStack.add(MainRoute.Home)
                        }
                    )
                }

                entry<MainRoute.ProviderHistory> {
                    ProviderHistoryScreen(
                        onOrderDetails = { orderId -> backStack.add(MainRoute.ProviderOrderDetails(orderId)) }
                    )
                }

                entry<MainRoute.ProviderOrderDetails> { route ->
                    ProviderOrderDetailsScreen(
                        orderId = route.orderId,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                // Customer Entries
                entry<MainRoute.CustomerHome> {
                    CustomerHomeScreen(
                         onOrderDetails = { orderId -> backStack.add(MainRoute.CustomerOrderDetails(orderId)) }
                    )
                }

                entry<MainRoute.CustomerHistory> {
                    CustomerHistoryScreen(
                        onOrderDetails = { orderId -> backStack.add(MainRoute.CustomerOrderDetails(orderId)) }
                    )
                }

                entry<MainRoute.CustomerSearch> {
                    CustomerSearchUserScreen()
                }

                entry<MainRoute.CustomerOrderDetails> { route ->
                    CustomerOrderDetailsScreen(
                        orderId = route.orderId,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                // Shipper Entries
                entry<MainRoute.ShipperHome> {
                    ShipperHomeScreen(
                        onOrderDetails = { orderId -> backStack.add(MainRoute.ShipperOrderDetails(orderId)) }
                    )
                }

                entry<MainRoute.ShipperFindOrder> {
                    ShipperFindOrderScreen(
                        onOrderDetails = { orderId -> backStack.add(MainRoute.ShipperOrderDetails(orderId)) }
                    )
                }

                entry<MainRoute.ShipperHistory> {
                    ShipperHistoryScreen(
                        onOrderDetails = { orderId -> backStack.add(MainRoute.ShipperOrderDetails(orderId)) }
                    )
                }

                entry<MainRoute.ShipperOrderDetails> { route ->
                    ShipperOrderDetailsScreen(
                        orderId = route.orderId,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}
