package com.thomas.cargotracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.ui.screens.admin.AdminCreateUserScreen
import com.thomas.cargotracker.ui.screens.admin.AdminHomeScreen
import com.thomas.cargotracker.ui.screens.admin.AdminUserListScreen
import com.thomas.cargotracker.ui.screens.customer.CustomerHistoryScreen
import com.thomas.cargotracker.ui.screens.customer.CustomerHomeScreen
import com.thomas.cargotracker.ui.screens.customer.CustomerOrderDetailsScreen
import com.thomas.cargotracker.ui.screens.customer.CustomerSearchProvidersScreen
import com.thomas.cargotracker.ui.screens.main.MainScreen
import com.thomas.cargotracker.ui.screens.profile.ChangePasswordScreen
import com.thomas.cargotracker.ui.screens.profile.EditProfileScreen
import com.thomas.cargotracker.ui.screens.profile.ProfileScreen
import com.thomas.cargotracker.ui.screens.provider.ProviderHistoryScreen
import com.thomas.cargotracker.ui.screens.provider.ProviderHomeScreen
import com.thomas.cargotracker.ui.screens.provider.ProviderPendingOrdersScreen
import com.thomas.cargotracker.ui.screens.provider.ProviderProvisionShipmentScreen
import com.thomas.cargotracker.ui.screens.provider.ProviderShipmentDetailsScreen
import com.thomas.cargotracker.ui.screens.shipper.ShipperFindOrderScreen
import com.thomas.cargotracker.ui.screens.shipper.ShipperHistoryScreen
import com.thomas.cargotracker.ui.screens.shipper.ShipperHomeScreen
import com.thomas.cargotracker.ui.screens.shipper.ShipperOrderDetailsScreen
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed class MainRoute : NavKey {
    @Serializable
    data object Home : MainRoute()
    @Serializable
    data object Profile : MainRoute()
    @Serializable
    data object EditProfile : MainRoute()
    @Serializable
    data object ChangePassword : MainRoute()

    // Provider
    @Serializable
    data object ProviderPendingOrders : MainRoute()
    @Serializable
    data class ProviderAcceptOrder(val orderId: String) : MainRoute()
    @Serializable
    data object ProviderHistory : MainRoute()
    @Serializable
    data class ProviderShipmentDetails(val shipmentId: String) : MainRoute()

    // Customer
    @Serializable
    data object CustomerHome : MainRoute()
    @Serializable
    data object CustomerHistory : MainRoute()
    @Serializable
    data object CustomerCreateOrder : MainRoute()
    @Serializable
    data object CustomerSearch : MainRoute()
    @Serializable
    data class CustomerOrderDetails(val orderId: String) : MainRoute()
    @Serializable
    data class CustomerShipmentDetails(val shipmentId: String) : MainRoute()

    // Shipper
    @Serializable
    data object ShipperHome : MainRoute()
    @Serializable
    data object ShipperFindOrder : MainRoute()
    @Serializable
    data object ShipperHistory : MainRoute()
    @Serializable
    data class ShipperOrderDetails(val orderId: String) : MainRoute()

    // Admin
    @Serializable
    data object AdminUserList : MainRoute()
    @Serializable
    data object AdminCreateUser : MainRoute()
}

@Composable
fun MainNavigation(
    authViewModel: AuthViewModel,
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
                            onShipmentDetails = { shipmentId -> backStack.add(MainRoute.ProviderShipmentDetails(shipmentId)) },
                            onAcceptOrder = { orderId -> backStack.add(MainRoute.ProviderAcceptOrder(orderId)) },
                            authViewModel = authViewModel
                        )
                        UserRole.CUSTOMER -> CustomerHomeScreen(
                            onOrderDetails = { orderId -> backStack.add(MainRoute.CustomerOrderDetails(orderId)) },
                            onShipmentDetails = { shipmentId -> backStack.add(MainRoute.CustomerShipmentDetails(shipmentId)) },
                            onCreateOrder = { backStack.add(MainRoute.CustomerCreateOrder) },
                            authViewModel = authViewModel
                        )
                        UserRole.SHIPPER -> ShipperHomeScreen(
                            onOrderDetails = { orderId -> backStack.add(MainRoute.ShipperOrderDetails(orderId)) },
                            authViewModel = authViewModel
                        )
                        UserRole.ADMIN -> AdminHomeScreen(
                            onManageUsers = { backStack.add(MainRoute.AdminUserList) },
                            onProfile = { backStack.add(MainRoute.Profile) },
                            authViewModel = authViewModel
                        )
                    }
                }

                entry<MainRoute.Profile> {
                    ProfileScreen(
                        userRole = userRole,
                        onLogout = onLogout,
                        onEditProfile = { backStack.add(MainRoute.EditProfile) },
                        onChangePassword = { backStack.add(MainRoute.ChangePassword) }
                    )
                }

                entry<MainRoute.EditProfile> {
                    EditProfileScreen(onBack = { backStack.removeLastOrNull() })
                }

                entry<MainRoute.ChangePassword> {
                    ChangePasswordScreen(onBack = { backStack.removeLastOrNull() })
                }

                // Provider Entries
                entry<MainRoute.ProviderPendingOrders> {
                    ProviderPendingOrdersScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onOrderProcessed = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                entry<MainRoute.ProviderAcceptOrder> { route ->
                    ProviderProvisionShipmentScreen(
                        orderId = route.orderId,
                        onShipmentProvisioned = {
                            backStack.clear()
                            backStack.add(MainRoute.Home)
                        }
                    )
                }

                entry<MainRoute.ProviderHistory> {
                    ProviderHistoryScreen(
                        onShipmentDetails = { shipmentId -> backStack.add(MainRoute.ProviderShipmentDetails(shipmentId)) }
                    )
                }

                entry<MainRoute.ProviderShipmentDetails> { route ->
                    ProviderShipmentDetailsScreen(
                        shipmentId = route.shipmentId,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                // Customer Entries
                entry<MainRoute.CustomerHome> {
                    CustomerHomeScreen(
                        onOrderDetails = { orderId -> backStack.add(MainRoute.CustomerOrderDetails(orderId)) },
                        onShipmentDetails = { shipmentId -> backStack.add(MainRoute.CustomerShipmentDetails(shipmentId)) },
                        onCreateOrder = { backStack.add(MainRoute.CustomerCreateOrder) },
                        authViewModel = authViewModel
                    )
                }

                entry<MainRoute.CustomerHistory> {
                    CustomerHistoryScreen(
                        onOrderDetails = { orderId -> backStack.add(MainRoute.CustomerOrderDetails(orderId)) }
                    )
                }

                entry<MainRoute.CustomerCreateOrder> {
                    com.thomas.cargotracker.ui.screens.customer.CustomerCreateOrderScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onOrderCreated = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                entry<MainRoute.CustomerSearch> {
                    CustomerSearchProvidersScreen()
                }

                entry<MainRoute.CustomerOrderDetails> { route ->
                    CustomerOrderDetailsScreen(
                        orderId = route.orderId,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<MainRoute.CustomerShipmentDetails> { route ->
                    ProviderShipmentDetailsScreen(
                        shipmentId = route.shipmentId,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                // Shipper Entries
                entry<MainRoute.ShipperHome> {
                    ShipperHomeScreen(
                        onOrderDetails = { orderId -> backStack.add(MainRoute.ShipperOrderDetails(orderId)) },
                        authViewModel = authViewModel
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

                // Admin Entries
                entry<MainRoute.AdminUserList> {
                    AdminUserListScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onCreateUser = { backStack.add(MainRoute.AdminCreateUser) }
                    )
                }

                entry<MainRoute.AdminCreateUser> {
                    AdminCreateUserScreen(
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}
