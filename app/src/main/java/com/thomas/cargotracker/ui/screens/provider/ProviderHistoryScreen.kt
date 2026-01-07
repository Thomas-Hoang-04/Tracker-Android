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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.ui.viewmodel.user.ProviderViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thomas.cargotracker.data.model.ShipmentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHistoryScreen(
    onShipmentDetails: (String) -> Unit,
    viewModel: ProviderViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<ShipmentStatus?>(null) } // null = All (Delivered + Cancelled)
    val allOrders by viewModel.shipments.collectAsState()
    
    // Refresh data on entry to ensure we have orders
    LaunchedEffect(Unit) {
        viewModel.loadShipments()
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Order History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Filter Section
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search ID, Description, Origin, Dest...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = selectedStatus == ShipmentStatus.DELIVERED,
                        onClick = { selectedStatus = ShipmentStatus.DELIVERED },
                        label = { Text("Delivered") }
                    )
                    FilterChip(
                        selected = selectedStatus == ShipmentStatus.CANCELLED,
                        onClick = { selectedStatus = ShipmentStatus.CANCELLED },
                        label = { Text("Cancelled") }
                    )
                }
            }

            // List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Base filter: Only terminal states (Delivered/Cancelled)
                // If the user wants to see "Active" they go to Home.
                val historyOrders = allOrders.filter { 
                    it.status == ShipmentStatus.DELIVERED || it.status == ShipmentStatus.CANCELLED 
                }

                val filteredOrders = historyOrders.filter { order ->
                    // 1. Status Filter
                    val matchesStatus = selectedStatus == null || order.status == selectedStatus
                    
                    // 2. Search Filter
                    val query = searchQuery.lowercase()
                    val matchesSearch = if (searchQuery.isBlank()) true else {
                        order.id.contains(query, ignoreCase = true) ||
                        order.customerName.contains(query, ignoreCase = true) ||
                        order.description.contains(query, ignoreCase = true) ||
                        order.origin.contains(query, ignoreCase = true) ||
                        order.destination.contains(query, ignoreCase = true)
                    }
                    
                    matchesStatus && matchesSearch
                }
                
                items(filteredOrders) { order ->
                    ProviderShipmentCard(
                        shipment = order,
                        onMoreDetailsClick = { onShipmentDetails(order.id) }
                    )
                }
                
                if (filteredOrders.isEmpty()) {
                    item {
                         Text(
                            text = if (searchQuery.isNotEmpty()) "No matching orders found." else "No history found.",
                            modifier = Modifier.padding(top = 16.dp, start = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
