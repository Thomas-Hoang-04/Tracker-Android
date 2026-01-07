package com.thomas.cargotracker.ui.screens.provider

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material3.Card
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.thomas.cargotracker.domain.model.SensorData
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.dto.OrderResponse
import com.thomas.cargotracker.ui.screens.customer.StatItem

@Composable
fun ProviderActionCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProviderShipmentCard(
    modifier: Modifier = Modifier,
    shipment: Shipment,
    onMoreDetailsClick: (() -> Unit)? = null,
    showStats: Boolean = true,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
) {
        OutlinedCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            // Subtle border
            border = border
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Shipment #${shipment.trackingId}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = shipment.customerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        // Order Status (Always Accepted for active shipments)
                        Surface(
                            color = Color(0xFFE8F5E9), // Light Green
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Order: ACCEPTED",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color(0xFF2E7D32) // Dark Green
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Shipment Status
                        val statusColor = when (shipment.status.name) {
                            "COMPLETED" -> Color(0xFFE8F5E9)
                            "IN_TRANSIT" -> Color(0xFFFFF3E0) // Light Orange
                            "ASSIGNED" -> Color(0xFFE3F2FD) // Light Blue
                            "PENDING" -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val contentColor = when (shipment.status.name) {
                            "COMPLETED" -> Color(0xFF2E7D32)
                            "IN_TRANSIT" -> Color(0xFFE65100) // Dark Orange
                            "ASSIGNED" -> Color(0xFF1565C0) // Dark Blue
                            "PENDING" -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Surface(
                            color = statusColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Shipment: ${shipment.status.name.replace("_", " ")}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Grid - Only show if data exists and is valid (not null/N/A)
                if (showStats && shipment.sensorData != null && shipment.sensorData.temperature != null) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            StatItem(label = "Product", value = shipment.description)
                            Spacer(modifier = Modifier.height(4.dp))
                            StatItem(
                                label = "Temp",
                                value = "${shipment.sensorData.temperature}°C"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatItem(
                                label = "Battery",
                                value = "${shipment.sensorData.batteryLevel}%",
                                valueColor = if ((shipment.sensorData.batteryLevel
                                        ?: 0) < 20
                                ) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            StatItem(label = "Created", value = shipment.createdDate.take(10))
                            Spacer(modifier = Modifier.height(4.dp))
                            StatItem(
                                label = "Humidity",
                                value = "${shipment.sensorData.humidity}%"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatItem(
                                label = "Updated",
                                value = shipment.sensorData.lastUpdated?.let {
                                    formatTimestamp(it)
                                } ?: "Just now"
                            )
                        }
                    }
                } else {
                     // Minimal details if no telemetry (cleaner view)
                     Column(modifier = Modifier.fillMaxWidth()) {
                            StatItem(label = "Product", value = shipment.description)
                            Spacer(modifier = Modifier.height(4.dp))
                            StatItem(label = "Created", value = shipment.createdDate.take(10))
                    }
                }

            Spacer(modifier = Modifier.height(8.dp))
            // Footer action
            if (onMoreDetailsClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onMoreDetailsClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "More details",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProviderPendingOrderCard(
    order: OrderResponse,
    onAccept: () -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order #${order.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Customer ID: ${order.customerId.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatItem(label = "Goods", value = order.goodsDescription)
                StatItem(label = "Pickup", value = order.pickupAddress)
                StatItem(label = "Delivery", value = order.deliveryAddress)
                if (order.specialRequirements != null) {
                    StatItem(label = "Requirements", value = order.specialRequirements)
                }
                if (order.requireTemperatureTracking) {
                    StatItem(
                        label = "Temperature",
                        value = "${order.minTemperature ?: "N/A"}°C - ${order.maxTemperature ?: "N/A"}°C"
                    )
                }
                if (order.requireHumidityTracking) {
                    StatItem(
                        label = "Humidity",
                        value = "${order.minHumidity ?: "N/A"}% - ${order.maxHumidity ?: "N/A"}%"
                    )
                }
                if (order.createdAt != null) {
                    StatItem(label = "Created", value = order.createdAt.take(10))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Accept")
                }
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("Reject")
                }
            }
        }
    }

    // Reject Dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = {
                showRejectDialog = false
                rejectReason = ""
            },
            title = { Text("Reject Order") },
            text = {
                Column {
                    Text("Please provide a reason for rejecting this order:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectReason.isNotBlank()) {
                            onReject(rejectReason)
                            showRejectDialog = false
                            rejectReason = ""
                        }
                    },
                    enabled = rejectReason.isNotBlank()
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRejectDialog = false
                        rejectReason = ""
                    }
                ) {
                    Text("Cancel")
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }
}

@Composable
fun LocationCard(
    latitude: Double?,
    longitude: Double?,
    isMoving: Boolean?,
    trackingId: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
            if (latitude != null && longitude != null) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$latitude, $longitude",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isMoving == true) {
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
                    val label = "Shipment #$trackingId"
                    val uri = "geo:0,0?q=$latitude,$longitude($label)".toUri()
                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    
                    try {
                        context.startActivity(mapIntent)
                    } catch (_: Exception) {
                        // Fallback if Google Maps app is not installed
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
            }
        }
    }
}

@Composable
fun TelemetryGrid(
    sensorData: SensorData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Temp & Humidity
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TelemetryCard(
                label = "Temperature",
                value = "${sensorData.temperature ?: "--"}°C",
                icon = Icons.Outlined.Thermostat,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                label = "Humidity",
                value = "${sensorData.humidity ?: "--"}%",
                icon = Icons.Outlined.WaterDrop,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: CO2 & Light
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TelemetryCard(
                label = "CO2",
                value = "${sensorData.co2?.toInt() ?: "--"} ppm",
                icon = Icons.Default.Cloud,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                label = "Light",
                value = "${sensorData.light?.toInt() ?: "--"} lux",
                icon = Icons.Default.WbSunny,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Signal & Lean
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TelemetryCard(
                label = "Signal",
                value = "${sensorData.signalStrength ?: "--"} dBm",
                icon = Icons.Default.SignalCellularAlt,
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                label = "Lean",
                value = "${sensorData.lean ?: "--"}°",
                icon = Icons.Default.ScreenRotation,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Last updated: ${formatTimestamp(sensorData.lastUpdated)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            maxLines = 2
        )
    }
}

@Composable
fun BatteryStatusCard(
    batteryLevel: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer // Distinguishable color
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryStd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Battery Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = if (batteryLevel != null) "$batteryLevel%" else "Unknown",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            // Visual indicator (simple)
            if (batteryLevel != null) {
                Surface(
                    color = when {
                        batteryLevel > 50 -> Color(0xFF4CAF50) // Green
                        batteryLevel > 20 -> Color(0xFFFFC107) // Amber
                        else -> Color(0xFFF44336) // Red
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.size(16.dp)
                ) {}
            }
        }
    }
}

fun formatTimestamp(isoString: String?): String {
    if (isoString == null) return "Unknown"
    return try {
        val instant = Instant.parse(isoString)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        zonedDateTime.format(formatter)
    } catch (_: Exception) {
        isoString // Fallback
    }
}

@Composable
fun TelemetryCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
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