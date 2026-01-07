package com.thomas.cargotracker.ui.screens.shipper

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.ui.viewmodel.user.ShipperViewModel
import com.thomas.cargotracker.ui.screens.provider.ProviderShipmentCard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel

@Composable
fun ShipperHomeScreen(
    onOrderDetails: (String) -> Unit,
    viewModel: ShipperViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val orders by viewModel.assignedOrders.collectAsState()
    val authState by authViewModel.authState.collectAsState()

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
                        text = "Hi, ${authState.currentUser?.fullName ?: "Shipper"}",
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
                        text = "Your Assigned Orders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Assigned Orders List
            items(orders) { order ->
                ProviderShipmentCard(
                    shipment = order,
                    onMoreDetailsClick = { onOrderDetails(order.id) },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                )
            }
            
            if (orders.isEmpty()) {
                item {
                    Text(
                        text = "No assigned orders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
