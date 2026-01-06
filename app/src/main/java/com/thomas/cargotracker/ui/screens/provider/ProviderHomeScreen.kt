package com.thomas.cargotracker.ui.screens.provider

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.data.model.ShipmentStatus
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel
import com.thomas.cargotracker.ui.viewmodel.user.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHomeScreen(
    providerId: String = "1000003767",
    onSeeOrderState: () -> Unit = {},
    onCreateOrder: () -> Unit = {},
    onOrderHistory: () -> Unit = {},
    onOrderDetails: (String) -> Unit = {},
    viewModel: ProviderViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val recentOrders by viewModel.orders.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Refresh orders on screen entry
    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
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
                        text = "Hi, ${authState.currentUser?.fullName ?: "Provider"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "ID: ${(authState.currentUser?.id ?: providerId).substringBefore("-")}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = filterState.search,
                        onValueChange = { viewModel.updateSearchFilter(it) },
                        label = { Text("Search Orders") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterState.status == null,
                            onClick = { viewModel.updateStatusFilter(null) },
                            label = { Text("All") }
                        )
                        ShipmentStatus.entries.forEach { status ->
                            FilterChip(
                                selected = filterState.status == status,
                                onClick = { viewModel.updateStatusFilter(status) },
                                label = { Text(status.name) }
                            )
                        }
                    }
                }
            }

            item {
                 Text(
                    text = "Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }


            // Orders List
            items(recentOrders) { order ->
                ProviderOrderCard(
                    order = order,
                    onMoreDetailsClick = { onOrderDetails(order.id) }
                )
            }
            
            if (recentOrders.isEmpty()) {
                item {
                    Text(
                        text = "No orders found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
