package com.thomas.cargotracker.ui.screens.shipper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.data.model.ShipmentStatus
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.ui.screens.provider.BatteryStatusCard
import com.thomas.cargotracker.ui.screens.provider.LocationCard
import com.thomas.cargotracker.ui.screens.provider.ProviderShipmentCard
import com.thomas.cargotracker.ui.screens.provider.TelemetryGrid
import com.thomas.cargotracker.ui.viewmodel.user.ShipperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperOrderDetailsScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: ShipperViewModel = hiltViewModel()
) {
    val assigned by viewModel.assignedOrders.collectAsState()
    val history by viewModel.historyOrders.collectAsState()
    
    // Search in both lists
    val order = assigned.find { it.id == orderId } 
        ?: history.find { it.id == orderId }
        ?: Shipment(
            id = orderId,
            trackingId = orderId.take(8),
            status = ShipmentStatus.PENDING,
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
             // Map Placeholder (Top)
             // Map View
            LocationCard(
                latitude = order.sensorData?.latitude,
                longitude = order.sensorData?.longitude,
                isMoving = order.sensorData?.isMoving,
                trackingId = order.trackingId,
                modifier = Modifier.fillMaxWidth()
            )

            // Route Info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Customer: ${order.customerName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Start: Warehouse A", style = MaterialTheme.typography.bodyMedium)
                    Text("Destination: ${order.customerName} HQ", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text("Live Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Detailed Stats
            ProviderShipmentCard(
                shipment = order,
                onMoreDetailsClick = { /* No-op */ },
                showStats = false
            )

            // Full Telemetry
            if (order.sensorData != null) {
                Text("Device Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                BatteryStatusCard(
                    batteryLevel = order.sensorData.batteryLevel,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Full Sensor Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TelemetryGrid(
                    sensorData = order.sensorData,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Actions
            if (order.status == ShipmentStatus.ASSIGNED) {
                Button(
                    onClick = { viewModel.startTransit(order.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Transit")
                }
            }
        }
    }
}
