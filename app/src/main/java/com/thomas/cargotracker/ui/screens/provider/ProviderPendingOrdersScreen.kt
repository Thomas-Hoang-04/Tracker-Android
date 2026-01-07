package com.thomas.cargotracker.ui.screens.provider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.dto.OrderResponse
import com.thomas.cargotracker.dto.OrderStatus
import com.thomas.cargotracker.ui.viewmodel.user.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPendingOrdersScreen(
    onBack: () -> Unit,
    onOrderProcessed: () -> Unit,
    viewModel: ProviderViewModel = hiltViewModel()
) {
    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val orderApprovalState by viewModel.orderApprovalState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedOrderForReject by remember { mutableStateOf<OrderResponse?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadPendingOrders()
    }

    LaunchedEffect(orderApprovalState) {
        when (val state = orderApprovalState) {
            is ProviderViewModel.OrderApprovalState.Accepted -> {
                snackbarHostState.showSnackbar("Order accepted - Shipment created")
                viewModel.resetOrderApprovalState()
            }
            is ProviderViewModel.OrderApprovalState.Rejected -> {
                snackbarHostState.showSnackbar("Order rejected")
                viewModel.resetOrderApprovalState()
            }
            is ProviderViewModel.OrderApprovalState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetOrderApprovalState()
            }
            else -> {}
        }
    }

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
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedOrderForReject?.let { order ->
                            viewModel.rejectOrder(order.id, rejectReason)
                        }
                        showRejectDialog = false
                        rejectReason = ""
                        selectedOrderForReject = null
                    },
                    enabled = rejectReason.length >= 5,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRejectDialog = false
                    rejectReason = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pending Orders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPendingOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            orderApprovalState is ProviderViewModel.OrderApprovalState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            pendingOrders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No pending orders",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Orders from customers will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingOrders) { order ->
                        PendingOrderCard(
                            order = order,
                            onAccept = { viewModel.acceptOrder(order.id) },
                            onReject = {
                                selectedOrderForReject = order
                                showRejectDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingOrderCard(
    order: OrderResponse,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order #${order.id.substringBefore("-")}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    order.status.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Description
            Text(
                order.goodsDescription,
                style = MaterialTheme.typography.bodyMedium
            )

            // Addresses
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "From: ${order.pickupAddress}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "To: ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Tracking Requirements
            if (order.requireTemperatureTracking || order.requireHumidityTracking || order.requireLocationTracking) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (order.requireTemperatureTracking) {
                        TrackingBadge(
                            icon = Icons.Default.Thermostat,
                            text = "${order.minTemperature?.toInt() ?: 0}°-${order.maxTemperature?.toInt() ?: 30}°C"
                        )
                    }
                    if (order.requireHumidityTracking) {
                        TrackingBadge(
                            icon = Icons.Default.WaterDrop,
                            text = "${order.minHumidity?.toInt() ?: 0}%-${order.maxHumidity?.toInt() ?: 100}%"
                        )
                    }
                    if (order.requireLocationTracking) {
                        TrackingBadge(
                            icon = Icons.Default.LocationOn,
                            text = "GPS"
                        )
                    }
                }
            }

            // Special Requirements
            order.specialRequirements?.let { requirements ->
                if (requirements.isNotBlank()) {
                    Text(
                        "Special: $requirements",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
private fun TrackingBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}