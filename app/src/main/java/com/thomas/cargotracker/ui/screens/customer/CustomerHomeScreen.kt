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
import com.thomas.cargotracker.data.model.ShipmentStatus
import com.thomas.cargotracker.dto.OrderStatus
import com.thomas.cargotracker.ui.viewmodel.user.CustomerViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel

@Composable
fun CustomerHomeScreen(
    onOrderDetails: (String) -> Unit,
    onShipmentDetails: (String) -> Unit,
    onCreateOrder: () -> Unit,
    viewModel: CustomerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val orders by viewModel.orders.collectAsState()
    val shipments by viewModel.shipments.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    
    // Pending/Rejected orders (chưa thành shipment)
    val pendingOrders = orders.filter { 
        it.status == OrderStatus.PENDING || it.status == OrderStatus.REJECTED 
    }
    
    // Active shipments (từ accepted orders, chưa delivered/cancelled)
    val activeShipments = shipments.filter { 
        it.status != ShipmentStatus.DELIVERED && it.status != ShipmentStatus.CANCELLED 
    }

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
                    text = "Active Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            // Pending/Rejected Orders (chưa được accept)
            items(pendingOrders) { order ->
                CustomerOrderCard(
                    order = order,
                    onMoreDetailsClick = { onOrderDetails(order.id) }
                )
            }
            
            // Active Shipments (đã được accept và đang vận chuyển)
            items(activeShipments) { shipment ->
                CustomerShipmentCard(
                    shipment = shipment,
                    onDetailsClick = { onShipmentDetails(shipment.id) }
                )
            }

            if (pendingOrders.isEmpty() && activeShipments.isEmpty()) {
                item {
                    Text(
                        text = "No active orders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
