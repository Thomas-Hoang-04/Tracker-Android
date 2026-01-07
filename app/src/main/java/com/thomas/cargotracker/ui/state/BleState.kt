package com.thomas.cargotracker.ui.state

import android.bluetooth.BluetoothDevice

enum class BleSetupState {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED,
    WAITING_FOR_REQUEST,
    TOKEN_REQUESTED,
    TOKEN_SENDING,
    TOKEN_CONFIRMED,
    THRESHOLD_REQUESTED,
    THRESHOLD_SENDING,
    THRESHOLD_CONFIRMED,
    DEVICE_READY,
    SETUP_COMPLETE,
    ERROR
}

data class BleScannedDevice(
    val device: BluetoothDevice?,
    val name: String?,
    val address: String,
    val rssi: Int
)

data class BleScanState(
    val isScanning: Boolean = false,
    val devices: List<BleScannedDevice> = emptyList(),
    val error: String? = null
)

data class BleProvisioningState(
    val setupState: BleSetupState = BleSetupState.DISCONNECTED,
    val connectedDevice: BleScannedDevice? = null,
    val deviceId: String? = null,
    val deviceMessage: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)

sealed class BleError(val message: String) {
    data object BluetoothDisabled : BleError("Bluetooth is disabled")
    data object PermissionDenied : BleError("Bluetooth permissions not granted")
    data object DeviceNotFound : BleError("Device not found")
    data object ConnectionFailed : BleError("Failed to connect to device")
    data object ServiceDiscoveryFailed : BleError("Failed to discover BLE services")
    data object WriteFailed : BleError("Failed to write to device")
    data object Timeout : BleError("Operation timed out")
    data class DeviceError(val deviceMessage: String) : BleError(deviceMessage)
    data class Unknown(val cause: String) : BleError("Unknown error: $cause")
}
