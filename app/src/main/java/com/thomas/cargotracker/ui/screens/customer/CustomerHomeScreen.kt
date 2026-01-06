package com.thomas.cargotracker.ui.screens.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.ui.viewmodel.user.CustomerViewModel
import com.thomas.cargotracker.ui.screens.provider.ProviderOrderCard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel

@Composable
fun CustomerHomeScreen(
    onOrderDetails: (String) -> Unit,
    onCreateOrder: () -> Unit,
    viewModel: CustomerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val orders by viewModel.orders.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val activeOrders = orders.filter { it.status.name != "DELIVERED" && it.status.name != "CANCELLED" }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = onCreateOrder,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                androidx.compose.material3.Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Order"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "Hi, ${authState.currentUser?.fullName ?: "Customer"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "ID: ${authState.currentUser?.id?.substringBefore("-") ?: ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Track your shipments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Text(
                    text = "Active Shipments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            // Recent Orders List (Active)
            items(activeOrders) { order ->
                ProviderOrderCard(
                    order = order,
                    onMoreDetailsClick = { onOrderDetails(order.id) }
                )
            }
            
            if (activeOrders.isEmpty()) {
                item {
                    Text(
                        text = "No active shipments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
