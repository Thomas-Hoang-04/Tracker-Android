package com.thomas.cargotracker.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.dto.OrderResponse
import com.thomas.cargotracker.dto.OrderStatus
import com.thomas.cargotracker.data.model.ShipmentStatus
import com.thomas.cargotracker.ui.viewmodel.user.CustomerViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import com.thomas.cargotracker.ui.screens.provider.ProviderShipmentCard
import com.thomas.cargotracker.ui.screens.provider.TelemetryGrid
import com.thomas.cargotracker.ui.screens.provider.LocationCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderDetailsScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()

    // Fetch order if not in list
    LaunchedEffect(orderId) {
        if (orders.none { it.id == orderId }) {
            viewModel.refreshOrders() // Refresh to get the order into the list
        }
    }

    // Auto-refresh for live telemetry
    LaunchedEffect(Unit) {
        while(true) {
            delay(30_000) // 30 seconds
            viewModel.refreshOrders() // Pull fresh data (telemetry updates)
        }
    }

    val order = orders.find { it.id == orderId } ?: OrderResponse(
        id = orderId,
        customerId = "",
        providerId = "",
        status = OrderStatus.PENDING,
        goodsDescription = "Loading...",
        pickupAddress = "",
        deliveryAddress = "",
        estimatedDeliveryAt = null,
        requireTemperatureTracking = false,
        minTemperature = null,
        maxTemperature = null,
        requireHumidityTracking = false,
        minHumidity = null,
        maxHumidity = null,
        requireLocationTracking = true,
        specialRequirements = null,
        shipmentId = null,
        rejectionReason = null,
        processedAt = null,
        createdAt = null,
        updatedAt = null
    )

    // Fetch related shipment if it exists
    val shipments by viewModel.shipments.collectAsState()
    val shipment = order.shipmentId?.let { id -> shipments.find { it.id == id } }

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
            // Map View
            LocationCard(
                latitude = shipment?.sensorData?.latitude,
                longitude = shipment?.sensorData?.longitude,
                isMoving = shipment?.sensorData?.isMoving,
                trackingId = order.id,
                modifier = Modifier.fillMaxWidth()
            )

            // Order Info Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Order #${order.id.take(8)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Provider ID: ${order.providerId.take(8)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Display Shipment Status if it's relevant to the customer (In Transit, Delivered, Cancelled)
                        // For Pending/Assigned shipments, we show "Accepted" from the Order status
                        val useShipmentStatus = shipment != null && 
                                              (shipment.status == ShipmentStatus.IN_TRANSIT || 
                                               shipment.status == ShipmentStatus.COMPLETED || 
                                               shipment.status == ShipmentStatus.CANCELLED)

                        val displayStatus = if (useShipmentStatus) shipment!!.status.name else order.status.name
                        
                        val statusColor = if (useShipmentStatus) {
                            when (shipment!!.status) {
                                ShipmentStatus.IN_TRANSIT -> Color(0xFFFFF3E0) // Orange
                                ShipmentStatus.COMPLETED -> Color(0xFFE8F5E9)
                                ShipmentStatus.CANCELLED -> Color(0xFFFFEBEE)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        } else {
                            when (order.status) {
                                OrderStatus.ACCEPTED -> Color(0xFFE8F5E9)
                                OrderStatus.REJECTED -> Color(0xFFFFEBEE)
                                OrderStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        }
                        
                        val contentColor = if (useShipmentStatus) {
                            when (shipment!!.status) {
                                ShipmentStatus.IN_TRANSIT -> Color(0xFFE65100)
                                ShipmentStatus.COMPLETED -> Color(0xFF2E7D32)
                                ShipmentStatus.CANCELLED -> Color(0xFFC62828)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        } else {
                            when (order.status) {
                                OrderStatus.ACCEPTED -> Color(0xFF2E7D32)
                                OrderStatus.REJECTED -> Color(0xFFC62828)
                                OrderStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        }

                        Surface(
                            color = statusColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = displayStatus.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = contentColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Goods: ${order.goodsDescription}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pickup: ${order.pickupAddress}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Delivery: ${order.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
                    if (order.specialRequirements != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Requirements: ${order.specialRequirements}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (order.rejectionReason != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Rejection Reason: ${order.rejectionReason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (order.createdAt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Created: ${order.createdAt.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Tracking Requirements
            if (order.requireTemperatureTracking || order.requireHumidityTracking || order.requireLocationTracking) {
                Text("Tracking Requirements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (order.requireTemperatureTracking) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Temperature", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${order.minTemperature ?: "N/A"}°C - ${order.maxTemperature ?: "N/A"}°C",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (order.requireHumidityTracking) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Humidity", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${order.minHumidity ?: "N/A"}% - ${order.maxHumidity ?: "N/A"}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (order.requireLocationTracking) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Location Tracking", style = MaterialTheme.typography.bodyMedium)
                                Text("Enabled", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (order.shipmentId != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Shipment ID", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    order.shipmentId.take(8),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Detailed Stats
            if (shipment != null) {
                ProviderShipmentCard(
                    shipment = shipment,
                    showStats = false
                )

                // Full Telemetry
                if (shipment.sensorData != null) {
                    Text("Full Sensor Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TelemetryGrid(
                        sensorData = shipment.sensorData!!,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Display Assigned Shipper Info
                if (shipment.shipperId != null) {
                     val shippers by viewModel.shippers.collectAsState()
                     val shipperName = shippers.find { it.id == shipment.shipperId }?.name ?: "Unknown Shipper"
                     
                     OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                         Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Assigned Shipper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = shipperName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID: ${shipment.shipperId}", 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                         }
                    }
                }
            }

            // Actions
            if (shipment != null && shipment.status == ShipmentStatus.IN_TRANSIT) {
                Button(
                    onClick = { viewModel.confirmDelivery(order.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Receipt")
                }
            } else if (order.status == OrderStatus.PENDING || (shipment != null && shipment.status == ShipmentStatus.ASSIGNED)) {
                // Allow cancel if Order is PENDING (though API support is limited, we show button)
                // OR if Shipment is ASSIGNED (before transit)
                OutlinedButton(
                    onClick = { viewModel.cancelShipment(order.id, "Cancelled by Customer") },
                    modifier = Modifier.fillMaxWidth(),
                    // Disable if it's PENDING because we don't have cancelOrder API implementation in ViewModel yet 
                    // (ViewModel checks for shipmentId so it would do nothing)
                    enabled = shipment != null 
                ) {
                    Text("Cancel Order")
                }
            }
        }
    }
}