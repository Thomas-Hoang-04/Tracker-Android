package com.thomas.cargotracker.ui.screens.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.ui.screens.provider.ProviderOrderCard
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thomas.cargotracker.ui.viewmodel.user.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderDetailsScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.id == orderId } ?: Shipment(
        id = orderId,
        trackingId = orderId.take(8),
        status = com.thomas.cargotracker.data.model.ShipmentStatus.PENDING, // Helper/Fallback 
        description = "Unknown",
        origin = "Unknown",
        destination = "Unknown",
        senderId = "Unknown",
        receiverId = "Unknown",
        customerName = "Unknown"
    )

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Track Order") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
             // Map Placeholder (Top) - mimicking design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                 colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f) // Placeholder color
                 )
            ) {
                 Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Map View Placeholder")
                }
            }

            // Route Info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order #${order.trackingId}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("From: ${order.origin}", style = MaterialTheme.typography.bodyMedium)
                    Text("To: ${order.destination}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text("Live Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Detailed Stats
            ProviderOrderCard(
                order = order,
                onMoreDetailsClick = { /* No-op */ }
            )
        }
    }
}
