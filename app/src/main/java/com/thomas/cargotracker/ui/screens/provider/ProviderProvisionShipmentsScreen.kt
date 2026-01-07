package com.thomas.cargotracker.ui.screens.provider

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.dto.ThresholdSettings
import com.thomas.cargotracker.ui.components.PrimaryButton
import com.thomas.cargotracker.ui.components.SecondaryButton
import com.thomas.cargotracker.ui.state.BleScannedDevice
import com.thomas.cargotracker.ui.state.BleSetupState
import com.thomas.cargotracker.ui.viewmodel.AuthViewModel
import com.thomas.cargotracker.ui.viewmodel.BleViewModel
import com.thomas.cargotracker.ui.viewmodel.user.ProviderViewModel



enum class ProvisionShipmentStep {
    SELECT_SHIPMENT,
    FIND_SHIPPER,
    CONNECT_DEVICE,
    SETUP_THRESHOLD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProvisionShipmentScreen(
    orderId: String? = null,
    onShipmentProvisioned: () -> Unit,
    viewModel: ProviderViewModel = hiltViewModel(),
    bleViewModel: BleViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    // If orderId is provided, start from FIND_SHIPPER (skip select shipment)
    var currentStep by remember { 
        mutableStateOf(
            if (orderId != null) ProvisionShipmentStep.FIND_SHIPPER 
            else ProvisionShipmentStep.SELECT_SHIPMENT
        ) 
    }

    var orderSearch by remember { mutableStateOf("") }
    var selectedOrder by remember { mutableStateOf<Shipment?>(null) }
    
    // Track if we've accepted the order
    var acceptedOrderId by remember { mutableStateOf<String?>(null) }
    val orderApprovalState by viewModel.orderApprovalState.collectAsState()

    var shipperSearch by remember { mutableStateOf("") }
    var selectedShipper by remember { mutableStateOf<ProviderViewModel.UserResult?>(null) }

    var selectedDevice by remember { mutableStateOf<BleScannedDevice?>(null) }
    var tokenInput by remember { mutableStateOf("") }

    var tempMin by remember { mutableStateOf("20.0") }
    var tempMax by remember { mutableStateOf("30.0") }
    var humidityMin by remember { mutableStateOf("30.0") }
    var humidityMax by remember { mutableStateOf("60.0") }
    var gasThreshold by remember { mutableStateOf("100") }
    var accelThreshold by remember { mutableStateOf("1.0") }
    var gyroThreshold by remember { mutableStateOf("1.0") }
    var readPeriodMs by remember { mutableStateOf("30000") }

    val shipments by viewModel.shipments.collectAsState()
    val unprovisionedOrders = remember(shipments) {
        shipments.filter { it.status.name == "PENDING" || (it.status.name == "ASSIGNED" && it.deviceId == null) }
    }
    val filteredOrders = if (orderSearch.isEmpty()) unprovisionedOrders else unprovisionedOrders.filter {
        it.id.contains(orderSearch, ignoreCase = true) || it.description.contains(orderSearch, ignoreCase = true)
    }

    val scanState by bleViewModel.scanState.collectAsState()
    val connectionState by bleViewModel.connectionState.collectAsState()
    val bleUiState by bleViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var permissionsGranted by remember { mutableStateOf(false) }
    var bluetoothEnabled by remember { mutableStateOf(bleViewModel.isBluetoothEnabled()) }
    var hasTriggeredScan by remember { mutableStateOf(false) }
    var showDeviceReadyDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var isProvisioningNewOrder by remember { mutableStateOf(false) }

    // Accept order when screen opens with orderId
    // REMOVED: Auto-accept logic. We now wait until the end.
    
    // Handle accept result - find the created shipment
    LaunchedEffect(orderApprovalState) {
        if (orderApprovalState is ProviderViewModel.OrderApprovalState.Accepted) {
            val state = orderApprovalState as ProviderViewModel.OrderApprovalState.Accepted
            if (isProvisioningNewOrder) {
                // Step 2: Order Accepted, now Provision
                val shipmentId = state.order.shipmentId
                if (shipmentId != null) {
                    viewModel.provisionOrder(
                        shipmentId = shipmentId,
                        deviceId = bleUiState.provisionedDeviceId ?: connectionState.deviceId ?: "",
                        shipperId = selectedShipper?.id
                    )
                } else {
                    // Fallback: Reload shipments and try to find it? 
                    // Or show error. Ideally shipmentId should be present.
                    snackbarHostState.showSnackbar("Error: Shipment ID missing from accepted order")
                    isProvisioningNewOrder = false
                }
            } else if (acceptedOrderId != null) {
                 // For legacy or other flows if needed
                 viewModel.loadShipments()
            }
        } else if (orderApprovalState is ProviderViewModel.OrderApprovalState.Error) {
             isProvisioningNewOrder = false
        }
    }
    


    LaunchedEffect(Unit) {
        bleViewModel.resetState()
        viewModel.loadShipments()
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        bluetoothEnabled = result.resultCode == Activity.RESULT_OK
        if (bluetoothEnabled && permissionsGranted && currentStep == ProvisionShipmentStep.CONNECT_DEVICE) {
            bleViewModel.startScan()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        onResult = @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN) { permissions ->
            permissionsGranted = permissions.values.all { it }
            if (permissionsGranted) {
                bluetoothEnabled = bleViewModel.isBluetoothEnabled()
                if (bluetoothEnabled && currentStep == ProvisionShipmentStep.CONNECT_DEVICE) {
                    bleViewModel.startScan()
                } else if (!bluetoothEnabled) {
                    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableIntent)
                }
            }
        }
    )

    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(currentStep) {
        if (currentStep == ProvisionShipmentStep.CONNECT_DEVICE && !hasTriggeredScan) {
            hasTriggeredScan = true
            bluetoothEnabled = bleViewModel.isBluetoothEnabled()
            permissionLauncher.launch(requiredPermissions)
        }
    }

    LaunchedEffect(bleUiState.error) {
        bleUiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            bleViewModel.clearError()
        }
    }

    LaunchedEffect(bleUiState.successMessage) {
        bleUiState.successMessage?.let { message ->
            if (!bleUiState.setupComplete) {
                snackbarHostState.showSnackbar(message)
            }
            bleViewModel.clearSuccess()
        }
    }

    LaunchedEffect(connectionState.setupState) {
        if (connectionState.setupState == BleSetupState.THRESHOLD_REQUESTED && currentStep == ProvisionShipmentStep.CONNECT_DEVICE) {
            currentStep = ProvisionShipmentStep.SETUP_THRESHOLD
        }
        if (connectionState.setupState == BleSetupState.DEVICE_READY && currentStep == ProvisionShipmentStep.CONNECT_DEVICE) {
            showDeviceReadyDialog = true
        }
    }

    val createOrderState by viewModel.createOrderState.collectAsState()

    val handleProvisionOrder = {
        // Get deviceId from BLE state OR from the connected device info
        // The deviceId is stored in TestDeviceData when using mock, or from actual device when using real BLE
        val deviceIdToUse = bleUiState.provisionedDeviceId 
            ?: connectionState.deviceId  // Try connection state
            ?: ""
        
        if (selectedOrder != null) {
            // Existing shipment flow
            viewModel.provisionOrder(
                shipmentId = selectedOrder!!.id,
                deviceId = deviceIdToUse,
                shipperId = selectedShipper?.id
            )
        } else if (orderId != null) {
            // New Order flow: Step 1 - Accept Order
            isProvisioningNewOrder = true
            viewModel.acceptOrder(orderId)
        }
    }

    LaunchedEffect(createOrderState) {
        when (val state = createOrderState) {
            is ProviderViewModel.CreateOrderState.Success -> {
                bleViewModel.disconnect()
                viewModel.resetCreateOrderState()
                onShipmentProvisioned()
                isProvisioningNewOrder = false
            }
            is ProviderViewModel.CreateOrderState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetCreateOrderState()
                isProvisioningNewOrder = false
            }
            else -> {}
        }
    }

    LaunchedEffect(bleUiState.setupComplete) {
        if (bleUiState.setupComplete && currentStep == ProvisionShipmentStep.SETUP_THRESHOLD) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Success") },
            text = { Text("Device provisioned successfully!", textAlign = TextAlign.Center) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        handleProvisionOrder()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showDeviceReadyDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Device Ready") },
            text = {
                Text(
                    "This device is already configured. Would you like to reconfigure the sensor thresholds or proceed with provisioning?",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeviceReadyDialog = false
                        currentStep = ProvisionShipmentStep.SETUP_THRESHOLD
                    }
                ) {
                    Text("Configure Thresholds")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeviceReadyDialog = false
                        handleProvisionOrder()
                    }
                ) {
                    Text("Skip & Provision")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getTitleForStep(currentStep),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (currentStep == ProvisionShipmentStep.CONNECT_DEVICE && !scanState.isScanning && connectionState.setupState == BleSetupState.DISCONNECTED) {
                        IconButton(onClick = {
                            bluetoothEnabled = bleViewModel.isBluetoothEnabled()
                            if (bluetoothEnabled) {
                                bleViewModel.startScan()
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (currentStep.ordinal + 1) / 4f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentStep) {
                ProvisionShipmentStep.SELECT_SHIPMENT -> {
                    Text("Select an order to provision with a device.")
                    OutlinedTextField(
                        value = orderSearch,
                        onValueChange = { orderSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search Order ID or Description") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredOrders) { order ->
                            Card(
                                onClick = { selectedOrder = order },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedOrder?.id == order.id)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Order #${order.id.substringBefore("-")}", fontWeight = FontWeight.Bold)
                                    Text(order.description, style = MaterialTheme.typography.bodyMedium)
                                    Text("From: ${order.origin}", style = MaterialTheme.typography.bodySmall)
                                    Text("To: ${order.destination}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        if (unprovisionedOrders.isEmpty()) {
                            item {
                                Text(
                                    text = "No pending orders found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                )
                            }
                        }
                    }

                    PrimaryButton(
                        text = "Next",
                        onClick = { currentStep = ProvisionShipmentStep.FIND_SHIPPER },
                        enabled = selectedOrder != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ProvisionShipmentStep.FIND_SHIPPER -> {
                    Text("Search for a shipper to transport this order (Optional).")
                    OutlinedTextField(
                        value = shipperSearch,
                        onValueChange = { shipperSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Shipper ID or Name") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    val shippers by viewModel.shippers.collectAsState()
                    val filteredShippers = if (shipperSearch.isEmpty()) shippers else shippers.filter {
                        it.name.contains(shipperSearch, ignoreCase = true) || it.id.contains(shipperSearch, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredShippers) { shipper ->
                            Card(
                                onClick = { selectedShipper = shipper },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedShipper?.id == shipper.id)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(shipper.name, fontWeight = FontWeight.Bold)
                                    Text("ID: ${shipper.id}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { currentStep = ProvisionShipmentStep.SELECT_SHIPMENT }) { Text("Back") }

                        PrimaryButton(
                            text = if (selectedShipper != null) "Next" else "Skip",
                            onClick = { currentStep = ProvisionShipmentStep.CONNECT_DEVICE },
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }

                ProvisionShipmentStep.CONNECT_DEVICE -> {
                    val isDeviceConnecting = connectionState.setupState == BleSetupState.CONNECTING
                    val isDeviceConnected = connectionState.setupState in listOf(
                        BleSetupState.CONNECTED,
                        BleSetupState.WAITING_FOR_REQUEST,
                        BleSetupState.TOKEN_REQUESTED,
                        BleSetupState.TOKEN_SENDING,
                        BleSetupState.TOKEN_CONFIRMED,
                        BleSetupState.THRESHOLD_REQUESTED,
                        BleSetupState.DEVICE_READY,
                        BleSetupState.SETUP_COMPLETE
                    )
                    val isTokenRequested = bleUiState.showTokenInput

                    if (selectedDevice != null && (isDeviceConnecting || isDeviceConnected)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        selectedDevice?.name ?: "Unknown Device",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        selectedDevice?.address ?: "",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                when {
                                    isDeviceConnecting -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    isDeviceConnected -> Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    when {
                        isTokenRequested -> {
                            Text("Enter Device Token", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("The device is requesting a token for authentication.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = tokenInput,
                                onValueChange = { tokenInput = it },
                                label = { Text("Token") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !bleUiState.isLoading,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            PrimaryButton(
                                text = "Send Token",
                                onClick = { bleViewModel.sendToken(tokenInput) },
                                isLoading = bleUiState.isLoading,
                                enabled = tokenInput.isNotBlank()
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        isDeviceConnecting -> {
                            Column(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Connecting to device...", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        connectionState.setupState == BleSetupState.WAITING_FOR_REQUEST || connectionState.setupState == BleSetupState.CONNECTED -> {
                            Column(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Waiting for device request...", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        !bluetoothEnabled && permissionsGranted -> {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.BluetoothDisabled, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Bluetooth is disabled", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Please enable Bluetooth to scan for devices", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    PrimaryButton(
                                        text = "Enable Bluetooth",
                                        onClick = {
                                            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                            enableBluetoothLauncher.launch(enableIntent)
                                        }
                                    )
                                }
                            }
                        }
                        else -> {
                            if (scanState.isScanning) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Scanning for devices...", style = MaterialTheme.typography.bodyLarge)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (scanState.devices.isEmpty() && !scanState.isScanning) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("No devices found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        SecondaryButton(
                                            text = "Scan Again",
                                            onClick = {
                                                bluetoothEnabled = bleViewModel.isBluetoothEnabled()
                                                if (bluetoothEnabled) {
                                                    bleViewModel.startScan()
                                                }
                                            }
                                        )
                                    }
                                }
                            } else if (scanState.devices.isNotEmpty()) {
                                Text(
                                    if (scanState.isScanning) "Discovered Devices" else "Available Devices",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(scanState.devices, key = { it.address }) { device ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    bleViewModel.stopScan()
                                                    selectedDevice = device
                                                    bleViewModel.connectToDevice(device)
                                                },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(device.name ?: "Unknown Device", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                                    Text(device.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Icon(
                                                    Icons.Default.SignalCellular4Bar,
                                                    contentDescription = "Signal strength",
                                                    tint = when {
                                                        device.rssi > -50 -> MaterialTheme.colorScheme.primary
                                                        device.rssi > -70 -> MaterialTheme.colorScheme.tertiary
                                                        else -> MaterialTheme.colorScheme.error
                                                    },
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            bleViewModel.disconnect()
                            selectedDevice = null
                            hasTriggeredScan = false
                            currentStep = ProvisionShipmentStep.FIND_SHIPPER
                        }) { Text("Back") }
                    }
                }

                ProvisionShipmentStep.SETUP_THRESHOLD -> {
                    Text(
                        text = "Smart Sensor Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f)
                    ) {
                        ThresholdCard(title = "Environment", icon = Icons.Outlined.Thermostat) {
                            Text("Temperature Range: ${tempMin}°C - ${tempMax}°C", style = MaterialTheme.typography.bodySmall)
                            RangeSlider(
                                value = (tempMin.toFloatOrNull() ?: 20f)..(tempMax.toFloatOrNull() ?: 30f),
                                onValueChange = { range ->
                                    tempMin = range.start.toInt().toString()
                                    tempMax = range.endInclusive.toInt().toString()
                                },
                                valueRange = -20f..60f,
                                steps = 79,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Humidity Range: ${humidityMin}% - ${humidityMax}%", style = MaterialTheme.typography.bodySmall)
                            RangeSlider(
                                value = (humidityMin.toFloatOrNull() ?: 30f)..(humidityMax.toFloatOrNull() ?: 60f),
                                onValueChange = { range ->
                                    humidityMin = range.start.toInt().toString()
                                    humidityMax = range.endInclusive.toInt().toString()
                                },
                                valueRange = 0f..100f,
                                steps = 99,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        ThresholdCard(title = "Motion & Gas", icon = Icons.Outlined.Speed) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = accelThreshold,
                                    onValueChange = { accelThreshold = it },
                                    label = { Text("Accel (g)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = gyroThreshold,
                                    onValueChange = { gyroThreshold = it },
                                    label = { Text("Gyro (°/s)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = gasThreshold,
                                onValueChange = { gasThreshold = it },
                                label = { Text("Gas Sensitivity (0-1000)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        ThresholdCard(title = "Reporting", icon = Icons.Outlined.Timer) {
                            Text("Read Period", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val periods = listOf("30s" to "30000", "1m" to "60000", "5m" to "300000")
                                periods.forEach { (label, value) ->
                                    FilterChip(
                                        selected = readPeriodMs == value,
                                        onClick = { readPeriodMs = value },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            currentStep = ProvisionShipmentStep.CONNECT_DEVICE
                        }) { Text("Back") }

                        Spacer(modifier = Modifier.width(16.dp))

                        PrimaryButton(
                            text = "Provision Order",
                            onClick = {
                                bleViewModel.sendThresholds(
                                    ThresholdSettings(
                                        tempMin = tempMin.toFloatOrNull() ?: 20.0f,
                                        tempMax = tempMax.toFloatOrNull() ?: 30.0f,
                                        humidityMin = humidityMin.toFloatOrNull() ?: 30.0f,
                                        humidityMax = humidityMax.toFloatOrNull() ?: 60.0f,
                                        gasThreshold = gasThreshold.toIntOrNull() ?: 100,
                                        accelThreshold = accelThreshold.toFloatOrNull() ?: 1.0f,
                                        gyroThreshold = gyroThreshold.toFloatOrNull() ?: 1.0f,
                                        readPeriodMs = readPeriodMs.toLongOrNull() ?: 30000L
                                    )
                                )
                            },
                            enabled = !bleUiState.isLoading,
                            isLoading = bleUiState.isLoading,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThresholdCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

private fun getTitleForStep(step: ProvisionShipmentStep): String {
    return when(step) {
        ProvisionShipmentStep.SELECT_SHIPMENT -> "Select Shipment"
        ProvisionShipmentStep.FIND_SHIPPER -> "Assign Shipper"
        ProvisionShipmentStep.CONNECT_DEVICE -> "Connect Device"
        ProvisionShipmentStep.SETUP_THRESHOLD -> "Setup Threshold"
    }
}