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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.data.model.OrderSummary
import com.thomas.cargotracker.dto.ThresholdSettings
import com.thomas.cargotracker.ui.components.PrimaryButton
import com.thomas.cargotracker.ui.components.SecondaryButton
import com.thomas.cargotracker.ui.state.BleScannedDevice
import com.thomas.cargotracker.ui.state.BleSetupState
import com.thomas.cargotracker.ui.viewmodel.BleViewModel
import com.thomas.cargotracker.ui.viewmodel.ProviderViewModel

data class MockCustomer(val name: String, val id: String, val role: String = "Customer")

enum class CreateOrderStep {
    FIND_CUSTOMER,
    ORDER_INFO,
    CONNECT_DEVICE,
    SETUP_THRESHOLD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderCreateOrderScreen(
    onOrderCreated: () -> Unit,
    viewModel: ProviderViewModel = hiltViewModel(),
    bleViewModel: BleViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableStateOf(CreateOrderStep.FIND_CUSTOMER) }
    
    var customerSearch by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<String?>(null) }
    
    var orderName by remember { mutableStateOf("") }
    var productType by remember { mutableStateOf("Container") }

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

    val scanState by bleViewModel.scanState.collectAsState()
    val connectionState by bleViewModel.connectionState.collectAsState()
    val bleUiState by bleViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var permissionsGranted by remember { mutableStateOf(false) }
    var bluetoothEnabled by remember { mutableStateOf(bleViewModel.isBluetoothEnabled()) }
    var hasTriggeredScan by remember { mutableStateOf(false) }
    var showDeviceReadyDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Reset BLE state when entering the screen
    LaunchedEffect(Unit) {
        bleViewModel.resetState()
    }


    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        bluetoothEnabled = result.resultCode == Activity.RESULT_OK
        if (bluetoothEnabled && permissionsGranted && currentStep == CreateOrderStep.CONNECT_DEVICE) {
            bleViewModel.startScan()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        onResult = @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN) { permissions ->
            permissionsGranted = permissions.values.all { it }
            if (permissionsGranted) {
                bluetoothEnabled = bleViewModel.isBluetoothEnabled()
                if (bluetoothEnabled && currentStep == CreateOrderStep.CONNECT_DEVICE) {
                    bleViewModel.startScan()
                } else if (!bluetoothEnabled) {
                    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableIntent)
                }
            }
        }
    )

    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    LaunchedEffect(currentStep) {
        if (currentStep == CreateOrderStep.CONNECT_DEVICE && !hasTriggeredScan) {
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
        if (connectionState.setupState == BleSetupState.THRESHOLD_REQUESTED && currentStep == CreateOrderStep.CONNECT_DEVICE) {
            currentStep = CreateOrderStep.SETUP_THRESHOLD
        }
        if (connectionState.setupState == BleSetupState.DEVICE_READY && currentStep == CreateOrderStep.CONNECT_DEVICE) {
            showDeviceReadyDialog = true
        }
    }

    val handleCreateOrder = {
        val newOrder = OrderSummary(
            id = "ORD-${System.currentTimeMillis() % 10000}",
            customerName = selectedCustomer?.substringBefore(" (ID") ?: "Unknown",
            productType = productType.ifEmpty { "General Cargo" },
            tempMin = tempMin,
            tempMax = tempMax,
            humidityMin = humidityMin,
            humidityMax = humidityMax
        )
        viewModel.createOrder(newOrder)
        bleViewModel.disconnect()
        onOrderCreated()
    }

    LaunchedEffect(bleUiState.setupComplete) {
        if (bleUiState.setupComplete && currentStep == CreateOrderStep.SETUP_THRESHOLD) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { }, // Force user to click OK
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
                        handleCreateOrder()
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
                    "This device is already configured. Would you like to reconfigure the sensor thresholds or proceed with creating the order?",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeviceReadyDialog = false
                        currentStep = CreateOrderStep.SETUP_THRESHOLD
                    }
                ) {
                    Text("Configure Thresholds")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeviceReadyDialog = false
                        handleCreateOrder()
                    }
                ) {
                    Text("Skip & Create Order")
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
                    if (currentStep == CreateOrderStep.CONNECT_DEVICE && !scanState.isScanning && connectionState.setupState == BleSetupState.DISCONNECTED) {
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
                CreateOrderStep.FIND_CUSTOMER -> {
                    Text("Search for a customer to assign this order to.")
                    OutlinedTextField(
                        value = customerSearch,
                        onValueChange = { customerSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Customer ID or Name") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                    
                    // Mock Data
                    val mockCustomers = remember {
                        listOf(
                            MockCustomer("Alice Smith", "1001"),
                            MockCustomer("Bob Jones", "1002"),
                            MockCustomer("Charlie Brown", "1003"),
                            MockCustomer("Diana Prince", "1004"),
                            MockCustomer("Evan Wright", "1005"),
                            MockCustomer("Frank Castle", "1006"),
                            MockCustomer("Grace Hopper", "1007")
                        )
                    }

                    // Filtered List
                    val filteredCustomers = if (customerSearch.isEmpty()) emptyList() else mockCustomers.filter {
                        it.name.contains(customerSearch, ignoreCase = true) || it.id.contains(customerSearch, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCustomers) { customer ->
                             Card(
                                onClick = { selectedCustomer = "${customer.name} (ID: ${customer.id})" },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedCustomer?.contains(customer.id) == true) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(customer.name, fontWeight = FontWeight.Bold)
                                    Text("ID: ${customer.id}", style = MaterialTheme.typography.bodySmall)
                                    Text("Role: ${customer.role}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        if (filteredCustomers.isEmpty() && customerSearch.isNotEmpty()) {
                            item {
                                Text(
                                    "No customers found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }
                    }

                    PrimaryButton(
                        text = "Next",
                        onClick = { currentStep = CreateOrderStep.ORDER_INFO },
                        enabled = selectedCustomer != null
                    )
                }
                
                CreateOrderStep.ORDER_INFO -> {
                    OutlinedTextField(
                        value = orderName,
                        onValueChange = { orderName = it },
                        label = { Text("Order Name") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                     OutlinedTextField(
                        value = productType,
                        onValueChange = { productType = it },
                        label = { Text("Type of Product") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                    
                    // Show selected Customer read-only
                     Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Customer", style = MaterialTheme.typography.labelLarge)
                            Text(selectedCustomer ?: "Unknown")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { currentStep = CreateOrderStep.FIND_CUSTOMER }) { Text("Back") }
                        Spacer(modifier = Modifier.weight(1f))
                        PrimaryButton(
                            text = "Next",
                            onClick = { currentStep = CreateOrderStep.CONNECT_DEVICE },
                            enabled = orderName.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                CreateOrderStep.CONNECT_DEVICE -> {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
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
                            Text(
                                "Enter Device Token",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "The device is requesting a token for authentication.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = tokenInput,
                                onValueChange = { tokenInput = it },
                                label = { Text("Token") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !bleUiState.isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
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
                                    Icon(
                                        Icons.Default.BluetoothDisabled,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Bluetooth is disabled",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Please enable Bluetooth to scan for devices",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
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
                                        Icon(
                                            Icons.AutoMirrored.Filled.BluetoothSearching,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "No devices found",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Bluetooth,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        device.name ?: "Unknown Device",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        device.address,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
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
                            currentStep = CreateOrderStep.ORDER_INFO
                        }) { Text("Back") }
                    }
                }
                
                CreateOrderStep.SETUP_THRESHOLD -> {
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
                        // Environment Card
                        ThresholdCard(title = "Environment", icon = Icons.Outlined.Thermostat) {
                            Text("Temperature Range: ${tempMin}°C - ${tempMax}°C", style = MaterialTheme.typography.bodySmall)
                            RangeSlider(
                                value = (tempMin.toFloatOrNull() ?: 20f)..(tempMax.toFloatOrNull() ?: 30f),
                                onValueChange = { range ->
                                    tempMin = range.start.toInt().toString()
                                    tempMax = range.endInclusive.toInt().toString()
                                },
                                valueRange = -20f..60f,
                                steps = 79, // 1 degree steps roughly
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

                        // Motion Sensors Card
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

                        // Configuration Card
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
                            // Don't disconnect, just go back to connection screen to view status or change device
                            currentStep = CreateOrderStep.CONNECT_DEVICE
                        }) { Text("Back") }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        PrimaryButton(
                            text = "Create Order",
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

private fun getTitleForStep(step: CreateOrderStep): String {
    return when(step) {
        CreateOrderStep.FIND_CUSTOMER -> "Find Customer"
        CreateOrderStep.ORDER_INFO -> "Create Order"
        CreateOrderStep.CONNECT_DEVICE -> "Connect Device"
        CreateOrderStep.SETUP_THRESHOLD -> "Setup Threshold"
    }
}
