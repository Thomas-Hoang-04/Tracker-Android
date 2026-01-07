package com.thomas.cargotracker.ui.screens.provider

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
    val orders by viewModel.shipments.collectAsState()
    val order = orders.find { it.id == shipmentId } ?: Shipment(
        id = shipmentId,
        trackingId = shipmentId.take(8),
        status = ShipmentStatus.PENDING,
        description = "Unknown",
        origin = "Unknown",
        destination = "Unknown",
        senderId = "Unknown",
        receiverId = "Unknown",
        customerName = "Unknown"
    )

    // Polling Mechanism
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.loadOrders() // Refresh data
            delay(30_000) // 30 seconds
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
            Text("Live Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                 Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (order.sensorData?.latitude != null && order.sensorData.longitude != null) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${order.sensorData.latitude}, ${order.sensorData.longitude}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (order.sensorData.isMoving == true) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("In Motion", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stationary", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val context = LocalContext.current
                        Button(onClick = { 
                            val lat = order.sensorData.latitude
                            val lng = order.sensorData.longitude
                            val label = "Shipment #${order.trackingId}"
                            val uri = "geo:0,0?q=$lat,$lng($label)".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                // Fallback if Google Maps app is not installed (e.g. open in browser or just remove package restriction)
                                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(fallbackIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "No map app found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Text("View on Map")
                        }
                    } else {
                        Icon(Icons.Default.LocationOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Location Unavailable", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (order.status == ShipmentStatus.PENDING) {
                            Text("Shipment hasn't started yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // --- Telemetry Grid ---
            if (order.sensorData != null) {
                Text("Sensor Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TelemetryCard(
                            label = "Temperature",
                            value = "${order.sensorData.temperature ?: "--"}°C",
                            icon = Icons.Outlined.Thermostat,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryCard(
                            label = "Humidity",
                            value = "${order.sensorData.humidity ?: "--"}%",
                            icon = Icons.Outlined.WaterDrop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TelemetryCard(
                            label = "Pressure",
                            value = "${order.sensorData.pressure ?: "--"} hPa",
                            icon = Icons.Outlined.Compress, // Or similar
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryCard(
                            label = "Signal",
                            value = "${order.sensorData.signalStrength ?: "--"} dBm",
                            icon = Icons.Default.SignalCellularAlt,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Text(
                    text = "Last updated: ${order.sensorData.lastUpdated ?: "Unknown"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }


            // Actions
            if (order.status != ShipmentStatus.DELIVERED && order.status != ShipmentStatus.CANCELLED) {
                Text("Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (order.status == ShipmentStatus.PENDING || order.status == ShipmentStatus.ASSIGNED) {
                        Button(onClick = { viewModel.startTransit(order.id) }) {
                            Text("Start Transit")
                        }
                    }
                    if (order.status == ShipmentStatus.IN_TRANSIT) {
                        Button(onClick = { viewModel.completeShipment(order.id) }) {
                            Text("Complete")
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.cancelShipment(order.id, "Cancelled by provider") }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
