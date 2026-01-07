package com.thomas.cargotracker.ui.screens.customer

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
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
    shipmentStatus: ShipmentStatus? = null,
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

                // Display Shipment Status if it's relevant to the customer (In Transit, Delivered, Cancelled)
                // For Pending/Assigned shipments, we show "Accepted" from the Order status
                val useShipmentStatus = shipmentStatus != null && 
                                      (shipmentStatus == ShipmentStatus.IN_TRANSIT || 
                                       shipmentStatus == ShipmentStatus.DELIVERED || 
                                       shipmentStatus == ShipmentStatus.CANCELLED)

                val displayStatus = if (useShipmentStatus) shipmentStatus!!.name else order.status.name
                
                val statusColor = if (useShipmentStatus) {
                    when (shipmentStatus) {
                         ShipmentStatus.IN_TRANSIT -> Color(0xFFFFF3E0) // Orange
                         ShipmentStatus.DELIVERED -> Color(0xFFE8F5E9)
                         ShipmentStatus.CANCELLED -> Color(0xFFFFEBEE)
                         else -> MaterialTheme.colorScheme.surfaceVariant // Should not happen given check
                    }
                } else {
                    when (order.status) {
                        OrderStatus.ACCEPTED -> Color(0xFFE8F5E9)
                        OrderStatus.REJECTED -> Color(0xFFFFEBEE)
                        OrderStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                    }
                }
                
                val contentColor = if (useShipmentStatus) {
                    when (shipmentStatus) {
                         ShipmentStatus.IN_TRANSIT -> Color(0xFFE65100)
                         ShipmentStatus.DELIVERED -> Color(0xFF2E7D32)
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

// Skeleton Components
@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "Simmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ), label = "ShimmerOffset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFD0D0D0),
                Color(0xFFA0A0A0),
                Color(0xFFD0D0D0),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
    .onGloballyPositioned {
        size = it.size
    }
}

@Composable
fun SkeletonOrderCard(
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.height(20.dp).fillMaxWidth(0.5f).shimmerEffect())
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.height(14.dp).fillMaxWidth(0.7f).shimmerEffect())
                }

                Box(
                    modifier = Modifier.size(60.dp, 24.dp).shimmerEffect()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Skeleton
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(modifier = Modifier.height(14.dp).fillMaxWidth().shimmerEffect())
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                 Box(modifier = Modifier.size(80.dp, 24.dp).shimmerEffect())
            }
        }
    }
}