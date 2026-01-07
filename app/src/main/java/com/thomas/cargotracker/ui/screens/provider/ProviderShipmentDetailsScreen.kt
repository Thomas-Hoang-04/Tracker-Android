package com.thomas.cargotracker.ui.screens.provider

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.data.model.ShipmentStatus
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.ui.viewmodel.user.ProviderViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderShipmentDetailsScreen(
    shipmentId: String,
    onBack: () -> Unit,
    viewModel: ProviderViewModel = hiltViewModel()
) {
    val selectedShipment by viewModel.selectedShipment.collectAsState()
    val order = selectedShipment ?: Shipment(
        id = shipmentId,
        trackingId = shipmentId.take(8),
        status = ShipmentStatus.PENDING,
        description = "Loading...",
        origin = "Loading...",
        destination = "Loading...",
        senderId = "Unknown",
        receiverId = "Unknown",
        customerName = "Loading..."
    )

    // Load full details (including telemetry) on entry and poll
    LaunchedEffect(shipmentId) {
        viewModel.loadShipmentDetails(shipmentId)
        while(true) {
            delay(30_000) // 30 seconds polling
            viewModel.loadShipmentDetails(shipmentId)
        }
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            // Header Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order #${order.trackingId}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Customer: ${order.customerName}", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Status: ${order.status}", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (order.createdDate.isNotEmpty()) {
                        Text("Created: ${order.createdDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

                // Shipper Info
            val shippers by viewModel.shippers.collectAsState()
            val shipperName = if (order.shipperId != null) {
                shippers.find { it.id == order.shipperId }?.name ?: "Unknown Shipper"
            } else null

            if (order.shipperId != null) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Assigned Shipper", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = shipperName ?: "Unknown",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ID: ${order.shipperId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Device Status Section ---
            if (order.deviceId != null) {
                Text("Connected Device", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Device ID", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.deviceId.take(8) + "...", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                        
                        // Battery Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val battery = order.sensorData?.batteryLevel ?: 0
                            Icon(
                                imageVector = when {
                                    battery > 80 -> Icons.Default.BatteryFull
                                    battery > 50 -> Icons.Default.Battery5Bar
                                    battery > 20 -> Icons.Default.Battery3Bar
                                    else -> Icons.Default.BatteryAlert
                                },
                                contentDescription = "Battery",
                                tint = if (battery < 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$battery%", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- Location Status Section ---
            LocationCard(
                latitude = order.sensorData?.latitude,
                longitude = order.sensorData?.longitude,
                isMoving = order.sensorData?.isMoving,
                trackingId = order.trackingId,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Telemetry Grid ---
            if (order.sensorData != null) {
                Text("Sensor Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TelemetryGrid(
                    sensorData = order.sensorData,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // Actions
            if (order.status == ShipmentStatus.PENDING || order.status == ShipmentStatus.ASSIGNED) {
                Text("Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.cancelShipment(order.id, "Cancelled by provider") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Shipment")
                    }
                }
            }
        }
    }
}


