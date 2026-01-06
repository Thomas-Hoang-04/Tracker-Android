package com.thomas.cargotracker.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.ui.screens.provider.ProviderOrderCard
import com.thomas.cargotracker.ui.viewmodel.user.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminShipmentListScreen(
    onBack: () -> Unit,
    onOrderDetails: (String) -> Unit,
    viewModel: ProviderViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearFilters()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Shipments") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No shipments found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { shipment ->
                    ProviderOrderCard(
                        order = shipment,
                        onMoreDetailsClick = { onOrderDetails(shipment.id) }
                    )
                }
            }
        }
    }
}
