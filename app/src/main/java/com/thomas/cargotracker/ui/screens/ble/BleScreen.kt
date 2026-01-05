package com.thomas.cargotracker.ui.screens.ble

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.thomas.cargotracker.dto.ThresholdSettings
import com.thomas.cargotracker.ui.components.PrimaryButton
import com.thomas.cargotracker.ui.components.SecondaryButton
import com.thomas.cargotracker.ui.state.BleScannedDevice
import com.thomas.cargotracker.ui.state.BleSetupState
import com.thomas.cargotracker.ui.viewmodel.BleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleScanScreen(
    viewModel: BleViewModel = hiltViewModel(),
    onDeviceSelected: (BleScannedDevice) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scanState by viewModel.scanState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var permissionsGranted by remember { mutableStateOf(false) }
    var bluetoothEnabled by remember { mutableStateOf(viewModel.isBluetoothEnabled()) }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        bluetoothEnabled = result.resultCode == Activity.RESULT_OK
        if (bluetoothEnabled && permissionsGranted) {
            viewModel.startScan()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
        if (permissionsGranted) {
            bluetoothEnabled = viewModel.isBluetoothEnabled()
            if (bluetoothEnabled) {
                viewModel.startScan()
            } else {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableIntent)
            }
        }
    }

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

    LaunchedEffect(Unit) {
        permissionLauncher.launch(requiredPermissions)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Device") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!scanState.isScanning) {
                        IconButton(onClick = { viewModel.startScan() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
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

            if (!bluetoothEnabled && permissionsGranted) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
            } else if (scanState.devices.isEmpty() && !scanState.isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                            onClick = { viewModel.startScan() }
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(scanState.devices, key = { it.address }) { device ->
                        DeviceListItem(
                            device = device,
                            onClick = {
                                viewModel.stopScan()
                                onDeviceSelected(device)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceListItem(
    device: BleScannedDevice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleProvisioningScreen(
    device: BleScannedDevice,
    viewModel: BleViewModel = hiltViewModel(),
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var tokenInput by remember { mutableStateOf("") }
    var tempMin by remember { mutableFloatStateOf(20.0f) }
    var tempMax by remember { mutableFloatStateOf(30.0f) }
    var humidityMin by remember { mutableFloatStateOf(30.0f) }
    var humidityMax by remember { mutableFloatStateOf(60.0f) }
    var gasThreshold by remember { mutableIntStateOf(100) }
    var accelThreshold by remember { mutableFloatStateOf(1.0f) }
    var gyroThreshold by remember { mutableFloatStateOf(1.0f) }
    var readPeriodMs by remember { mutableLongStateOf(30000L) }

    LaunchedEffect(device) {
        viewModel.connectToDevice(device)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(uiState.setupComplete) {
        if (uiState.setupComplete) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Setup") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.disconnect()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DeviceInfoCard(device = device, state = connectionState.setupState)

            Spacer(modifier = Modifier.height(24.dp))

            when (connectionState.setupState) {
                BleSetupState.CONNECTING -> {
                    LoadingContent("Connecting to device...")
                }
                BleSetupState.CONNECTED, BleSetupState.WAITING_FOR_REQUEST -> {
                    LoadingContent("Waiting for device request...")
                }
                BleSetupState.TOKEN_REQUESTED, BleSetupState.TOKEN_SENDING -> {
                    TokenInputSection(
                        token = tokenInput,
                        onTokenChange = { tokenInput = it },
                        onSubmit = { viewModel.sendToken(tokenInput) },
                        isLoading = uiState.isLoading
                    )
                }
                BleSetupState.TOKEN_CONFIRMED -> {
                    SuccessMessage("Token saved successfully!")
                    LoadingContent("Waiting for next request...")
                }
                BleSetupState.THRESHOLD_REQUESTED, BleSetupState.THRESHOLD_SENDING -> {
                    ThresholdInputSection(
                        tempMin = tempMin, onTempMinChange = { tempMin = it },
                        tempMax = tempMax, onTempMaxChange = { tempMax = it },
                        humidityMin = humidityMin, onHumidityMinChange = { humidityMin = it },
                        humidityMax = humidityMax, onHumidityMaxChange = { humidityMax = it },
                        gasThreshold = gasThreshold, onGasThresholdChange = { gasThreshold = it },
                        accelThreshold = accelThreshold, onAccelThresholdChange = { accelThreshold = it },
                        gyroThreshold = gyroThreshold, onGyroThresholdChange = { gyroThreshold = it },
                        readPeriodMs = readPeriodMs, onReadPeriodChange = { readPeriodMs = it },
                        onSubmit = {
                            viewModel.sendThresholds(
                                ThresholdSettings(
                                    tempMin = tempMin,
                                    tempMax = tempMax,
                                    humidityMin = humidityMin,
                                    humidityMax = humidityMax,
                                    gasThreshold = gasThreshold,
                                    accelThreshold = accelThreshold,
                                    gyroThreshold = gyroThreshold,
                                    readPeriodMs = readPeriodMs
                                )
                            )
                        },
                        isLoading = uiState.isLoading
                    )
                }
                BleSetupState.DEVICE_READY, BleSetupState.SETUP_COMPLETE -> {
                    SetupCompleteContent(onDone = onComplete)
                }
                BleSetupState.ERROR -> {
                    ErrorContent(
                        message = connectionState.message ?: "An error occurred",
                        onRetry = { viewModel.connectToDevice(device) }
                    )
                }
                else -> {
                    LoadingContent("Initializing...")
                }
            }
        }
    }
}

@Composable
fun DeviceInfoCard(device: BleScannedDevice, state: BleSetupState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.name ?: "Unknown Device",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    device.address,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            StateIndicator(state = state)
        }
    }
}

@Composable
fun StateIndicator(state: BleSetupState) {
    val (icon, color) = when (state) {
        BleSetupState.CONNECTED, BleSetupState.WAITING_FOR_REQUEST,
        BleSetupState.TOKEN_CONFIRMED, BleSetupState.THRESHOLD_CONFIRMED,
        BleSetupState.DEVICE_READY, BleSetupState.SETUP_COMPLETE ->
            Icons.Default.Check to MaterialTheme.colorScheme.primary
        BleSetupState.ERROR ->
            Icons.Default.Close to MaterialTheme.colorScheme.error
        else -> null to MaterialTheme.colorScheme.onSurfaceVariant
    }

    if (state == BleSetupState.CONNECTING || state == BleSetupState.TOKEN_SENDING || state == BleSetupState.THRESHOLD_SENDING) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
    } else if (icon != null) {
        Icon(icon, contentDescription = null, tint = color)
    }
}

@Composable
fun LoadingContent(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SuccessMessage(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.shapes.medium
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(message, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun TokenInputSection(
    token: String,
    onTokenChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
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
        value = token,
        onValueChange = onTokenChange,
        label = { Text("Token") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isLoading,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )

    Spacer(modifier = Modifier.height(24.dp))

    PrimaryButton(
        text = "Send Token",
        onClick = onSubmit,
        isLoading = isLoading,
        enabled = token.isNotBlank()
    )
}

@Composable
fun ThresholdInputSection(
    tempMin: Float, onTempMinChange: (Float) -> Unit,
    tempMax: Float, onTempMaxChange: (Float) -> Unit,
    humidityMin: Float, onHumidityMinChange: (Float) -> Unit,
    humidityMax: Float, onHumidityMaxChange: (Float) -> Unit,
    gasThreshold: Int, onGasThresholdChange: (Int) -> Unit,
    accelThreshold: Float, onAccelThresholdChange: (Float) -> Unit,
    gyroThreshold: Float, onGyroThresholdChange: (Float) -> Unit,
    readPeriodMs: Long, onReadPeriodChange: (Long) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    Text(
        "Configure Thresholds",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        "Set sensor thresholds for the device.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    ThresholdField("Temperature Min (°C)", tempMin.toString()) { onTempMinChange(it.toFloatOrNull() ?: tempMin) }
    ThresholdField("Temperature Max (°C)", tempMax.toString()) { onTempMaxChange(it.toFloatOrNull() ?: tempMax) }
    ThresholdField("Humidity Min (%)", humidityMin.toString()) { onHumidityMinChange(it.toFloatOrNull() ?: humidityMin) }
    ThresholdField("Humidity Max (%)", humidityMax.toString()) { onHumidityMaxChange(it.toFloatOrNull() ?: humidityMax) }
    ThresholdField("Gas Threshold", gasThreshold.toString()) { onGasThresholdChange(it.toIntOrNull() ?: gasThreshold) }
    ThresholdField("Accelerometer Threshold", accelThreshold.toString()) { onAccelThresholdChange(it.toFloatOrNull() ?: accelThreshold) }
    ThresholdField("Gyroscope Threshold", gyroThreshold.toString()) { onGyroThresholdChange(it.toFloatOrNull() ?: gyroThreshold) }
    ThresholdField("Read Period (ms)", readPeriodMs.toString()) { onReadPeriodChange(it.toLongOrNull() ?: readPeriodMs) }

    Spacer(modifier = Modifier.height(24.dp))

    PrimaryButton(
        text = "Save Thresholds",
        onClick = onSubmit,
        isLoading = isLoading
    )
}

@Composable
fun ThresholdField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SetupCompleteContent(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Setup Complete!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Device has been provisioned successfully.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        PrimaryButton(text = "Done", onClick = onDone)
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Error",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        PrimaryButton(text = "Retry", onClick = onRetry)
    }
}
