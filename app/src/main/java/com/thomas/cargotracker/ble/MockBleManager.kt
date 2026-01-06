package com.thomas.cargotracker.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.thomas.cargotracker.dto.BleRequestCode
import com.thomas.cargotracker.dto.BleResponseCode
import com.thomas.cargotracker.dto.ThresholdSettings
import com.thomas.cargotracker.ui.state.BleError
import com.thomas.cargotracker.ui.state.BleScannedDevice
import com.thomas.cargotracker.ui.state.BleSetupState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockBleManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _scanState = MutableStateFlow(BleManager.ScanState())
    val scanState: StateFlow<BleManager.ScanState> = _scanState.asStateFlow()

    private val _connectionState = MutableStateFlow(BleManager.ConnectionState())
    val connectionState: StateFlow<BleManager.ConnectionState> = _connectionState.asStateFlow()

    private val _deviceRequest = MutableSharedFlow<BleRequestCode>()
    val deviceRequest = _deviceRequest.asSharedFlow()

    private val _deviceResponse = MutableSharedFlow<Pair<BleResponseCode, String?>>()
    val deviceResponse = _deviceResponse.asSharedFlow()

    private val _error = MutableSharedFlow<BleError>()
    val error = _error.asSharedFlow()

    private var mockBluetoothEnabled = true

    private data class MockDeviceState(
        val device: BleScannedDevice,
        var hasToken: Boolean,
        var hasThreshold: Boolean
    )

    private val mockDeviceStates = mutableMapOf(
        "AA:BB:CC:DD:EE:01" to MockDeviceState(
            device = BleScannedDevice(
                device = null as BluetoothDevice?,
                name = "ESP32-New-Device",
                address = "AA:BB:CC:DD:EE:01",
                rssi = -45
            ),
            hasToken = false,
            hasThreshold = false
        ),
        "AA:BB:CC:DD:EE:02" to MockDeviceState(
            device = BleScannedDevice(
                device = null as BluetoothDevice?,
                name = "ESP32-Has-Token",
                address = "AA:BB:CC:DD:EE:02",
                rssi = -62
            ),
            hasToken = true,
            hasThreshold = false
        ),
        "AA:BB:CC:DD:EE:03" to MockDeviceState(
            device = BleScannedDevice(
                device = null as BluetoothDevice?,
                name = "ESP32-Ready",
                address = "AA:BB:CC:DD:EE:03",
                rssi = -78
            ),
            hasToken = true,
            hasThreshold = true
        )
    )

    private val mockDevices: List<BleScannedDevice>
        get() = mockDeviceStates.values.map { it.device }

    private var connectedDeviceAddress: String? = null

    fun startScan() {
        if (!isBluetoothEnabled()) {
            scope.launch { _error.emit(BleError.BluetoothDisabled) }
            return
        }

        _scanState.update { BleManager.ScanState(isScanning = true, devices = emptyList()) }

        scope.launch {
            mockDevices.forEachIndexed { index, device ->
                delay(500L + (index * 300L))
                _scanState.update { state ->
                    state.copy(devices = state.devices + device)
                }
            }
            delay(1000)
            _scanState.update { it.copy(isScanning = false) }
        }
    }

    fun stopScan() {
        _scanState.update { it.copy(isScanning = false) }
    }

    fun connect(device: BleScannedDevice) {
        if (!isBluetoothEnabled()) {
            scope.launch { _error.emit(BleError.BluetoothDisabled) }
            return
        }

        val deviceState = mockDeviceStates[device.address]
        if (deviceState == null) {
            scope.launch { _error.emit(BleError.DeviceNotFound) }
            return
        }

        connectedDeviceAddress = device.address
        stopScan()

        _connectionState.update {
            BleManager.ConnectionState(setupState = BleSetupState.CONNECTING, connectedDevice = device)
        }

        scope.launch {
            delay(1500)
            _connectionState.update { it.copy(setupState = BleSetupState.CONNECTED) }
            delay(500)
            _connectionState.update { it.copy(setupState = BleSetupState.WAITING_FOR_REQUEST) }
            delay(1000)

            when {
                !deviceState.hasToken -> {
                    _connectionState.update {
                        it.copy(setupState = BleSetupState.TOKEN_REQUESTED, message = "Device needs token")
                    }
                    _deviceRequest.emit(BleRequestCode.REQUEST_TOKEN)
                }
                !deviceState.hasThreshold -> {
                    _connectionState.update {
                        it.copy(setupState = BleSetupState.THRESHOLD_REQUESTED, message = "Device needs threshold settings")
                    }
                    _deviceRequest.emit(BleRequestCode.REQUEST_THRESHOLD)
                }
                else -> {
                    _connectionState.update {
                        it.copy(setupState = BleSetupState.DEVICE_READY, message = "Device is ready")
                    }
                    _deviceRequest.emit(BleRequestCode.DEVICE_READY)
                }
            }
        }
    }

    fun sendToken(token: String) {
        val deviceState = connectedDeviceAddress?.let { mockDeviceStates[it] }
        if (deviceState == null) {
            scope.launch { _error.emit(BleError.WriteFailed) }
            return
        }
        _connectionState.update { it.copy(setupState = BleSetupState.TOKEN_SENDING) }

        scope.launch {
            delay(1000)
            deviceState.hasToken = true
            _connectionState.update { it.copy(setupState = BleSetupState.TOKEN_CONFIRMED, message = "Token saved successfully") }
            _deviceResponse.emit(BleResponseCode.TOKEN_OK to "Token saved successfully")

            delay(1000)
            if (!deviceState.hasThreshold) {
                _connectionState.update {
                    it.copy(setupState = BleSetupState.THRESHOLD_REQUESTED, message = "Device needs threshold settings")
                }
                _deviceRequest.emit(BleRequestCode.REQUEST_THRESHOLD)
            }
        }
    }

    fun sendThresholds(thresholds: ThresholdSettings) {
        val deviceState = connectedDeviceAddress?.let { mockDeviceStates[it] }
        if (deviceState == null) {
            scope.launch { _error.emit(BleError.WriteFailed) }
            return
        }
        _connectionState.update { it.copy(setupState = BleSetupState.THRESHOLD_SENDING) }

        scope.launch {
            delay(1000)
            deviceState.hasThreshold = true
            _connectionState.update { it.copy(setupState = BleSetupState.THRESHOLD_CONFIRMED, message = "Thresholds saved successfully") }
            _deviceResponse.emit(BleResponseCode.THRESHOLD_OK to "Thresholds saved successfully")

            delay(500)
            _connectionState.update { it.copy(setupState = BleSetupState.DEVICE_READY, message = "Device is ready") }
            _deviceRequest.emit(BleRequestCode.DEVICE_READY)
        }
    }

    fun disconnect() {
        connectedDeviceAddress = null
        _connectionState.update { BleManager.ConnectionState() }
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled ?: mockBluetoothEnabled

    fun setBluetoothEnabled(enabled: Boolean) {
        mockBluetoothEnabled = enabled
    }

    fun resetMockState() {
        connectedDeviceAddress = null
        mockDeviceStates["AA:BB:CC:DD:EE:01"]?.apply { hasToken = false; hasThreshold = false }
        mockDeviceStates["AA:BB:CC:DD:EE:02"]?.apply { hasToken = true; hasThreshold = false }
        mockDeviceStates["AA:BB:CC:DD:EE:03"]?.apply { hasToken = true; hasThreshold = true }
        _scanState.update { BleManager.ScanState() }
        _connectionState.update { BleManager.ConnectionState() }
        mockBluetoothEnabled = true
    }
}
