package com.thomas.cargotracker.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.ui.navigation.MainRoute

sealed class MainNavItem(
    val route: MainRoute,
    val label: String,
    val icon: ImageVector
) {
    data object Home : MainNavItem(MainRoute.Home, "Home", Icons.Default.Home)
    data object Profile : MainNavItem(MainRoute.Profile, "Profile", Icons.Default.AccountBox)

    data object ProviderPending : MainNavItem(MainRoute.ProviderPendingOrders, "Pending", Icons.Default.Build)
    data object ProviderHistory : MainNavItem(MainRoute.ProviderHistory, "History", Icons.Default.History)

    // Customer Items
    data object CustomerHome : MainNavItem(MainRoute.CustomerHome, "Home", Icons.Default.Home)
    data object CustomerHistory : MainNavItem(MainRoute.CustomerHistory, "History", Icons.Default.History)
    data object CustomerSearch : MainNavItem(MainRoute.CustomerSearch, "Search", Icons.Default.Search)

    // Shipper Items
    data object ShipperHome : MainNavItem(MainRoute.ShipperHome, "Home", Icons.Default.Home)
    data object ShipperFindOrder : MainNavItem(MainRoute.ShipperFindOrder, "Find Order", Icons.Default.Search)
    data object ShipperHistory : MainNavItem(MainRoute.ShipperHistory, "History", Icons.Default.History)

    companion object {
        fun getNavItemsForRole(role: UserRole): List<MainNavItem> {
            return when (role) {
                UserRole.PROVIDER -> listOf(Home, ProviderPending, ProviderHistory, Profile)
                UserRole.CUSTOMER -> listOf(CustomerHome, CustomerHistory, CustomerSearch, Profile)
                UserRole.SHIPPER -> listOf(ShipperHome, ShipperFindOrder, ShipperHistory, Profile)
                else -> listOf(Home, Profile)
            }
        }
    }
}



@Composable
fun MainScreen(
    userRole: UserRole,
    backStack: NavBackStack<NavKey>,
    content: @Composable () -> Unit
) {
    val navItems = MainNavItem.getNavItemsForRole(userRole)
    val currentRoute = backStack.lastOrNull() ?: MainRoute.Home

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navItems.forEach { item ->
                item(
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            backStack.clear()
                            backStack.add(item.route)
                        }
                    },
                    icon = {
                        Icon(
                            item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = { Text(item.label) }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}

