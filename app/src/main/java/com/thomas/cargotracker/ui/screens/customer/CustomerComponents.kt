package com.thomas.cargotracker.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thomas.cargotracker.data.model.ShipmentStatus
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.dto.OrderResponse
import com.thomas.cargotracker.dto.OrderStatus

@Composable
fun CustomerOrderCard(
    order: OrderResponse,
    onMoreDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                        text = "Order #${order.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Provider ID: ${order.providerId.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = when (order.status) {
                        OrderStatus.ACCEPTED -> Color(0xFFE8F5E9) // Light Green
                        OrderStatus.REJECTED -> Color(0xFFFFEBEE) // Light Red
                        OrderStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when (order.status) {
                            OrderStatus.ACCEPTED -> Color(0xFF2E7D32) // Dark Green
                            OrderStatus.REJECTED -> Color(0xFFC62828) // Dark Red
                            OrderStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
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
                if (order.createdAt != null) {
                    StatItem(label = "Created", value = order.createdAt.take(10))
                }
                if (order.rejectionReason != null) {
                    StatItem(
                        label = "Rejection Reason",
                        value = order.rejectionReason,
                        valueColor = MaterialTheme.colorScheme.error
                    )
                }
                if (order.shipmentId != null) {
                    StatItem(
                        label = "Shipment ID",
                        value = order.shipmentId.take(8),
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer action
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

@Composable
fun StatItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            maxLines = 2
        )
    }
}

@Composable
fun CustomerShipmentCard(
    shipment: Shipment,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with tracking ID and status
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
                }

                // Status chip with color based on ShipmentStatus
                Surface(
                    color = when (shipment.status) {
                        ShipmentStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                        ShipmentStatus.ASSIGNED -> Color(0xFFE3F2FD) // Light Blue
                        ShipmentStatus.IN_TRANSIT -> Color(0xFFFFF3E0) // Light Orange
                        ShipmentStatus.DELIVERED -> Color(0xFFE8F5E9) // Light Green
                        ShipmentStatus.CANCELLED -> Color(0xFFFFEBEE) // Light Red
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = shipment.status.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when (shipment.status) {
                            ShipmentStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                            ShipmentStatus.ASSIGNED -> Color(0xFF1565C0) // Dark Blue
                            ShipmentStatus.IN_TRANSIT -> Color(0xFFE65100) // Dark Orange
                            ShipmentStatus.DELIVERED -> Color(0xFF2E7D32) // Dark Green
                            ShipmentStatus.CANCELLED -> Color(0xFFC62828) // Dark Red
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Shipment Details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatItem(label = "Goods", value = shipment.description)
                StatItem(label = "From", value = shipment.origin)
                StatItem(label = "To", value = shipment.destination)
                if (shipment.createdDate.isNotEmpty()) {
                    StatItem(label = "Created", value = shipment.createdDate.take(10))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDetailsClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Track Shipment",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}