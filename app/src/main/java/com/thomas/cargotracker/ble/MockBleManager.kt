package com.thomas.cargotracker.ble

import android.bluetooth.BluetoothDevice
import com.thomas.cargotracker.dto.BleRequestCode
import com.thomas.cargotracker.dto.BleResponseCode
import com.thomas.cargotracker.dto.ThresholdSettings
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
class MockBleManager @Inject constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _scanState = MutableStateFlow(BleManager.ScanState())
    val scanState: StateFlow<BleManager.ScanState> = _scanState.asStateFlow()

    private val _connectionState = MutableStateFlow(BleManager.ConnectionState())
    val connectionState: StateFlow<BleManager.ConnectionState> = _connectionState.asStateFlow()

    private val _deviceRequest = MutableSharedFlow<BleRequestCode>()
    val deviceRequest = _deviceRequest.asSharedFlow()

    private val _deviceResponse = MutableSharedFlow<Pair<BleResponseCode, String?>>()
    val deviceResponse = _deviceResponse.asSharedFlow()

    private val _error = MutableSharedFlow<com.thomas.cargotracker.ui.state.BleError>()
    val error = _error.asSharedFlow()

    private var hasToken = false
    private var hasThreshold = false

    private val mockDevices = listOf(
        createMockDevice("ESP32-IoT-Device-001", "AA:BB:CC:DD:EE:01", -45),
        createMockDevice("ESP32-IoT-Device-002", "AA:BB:CC:DD:EE:02", -62),
        createMockDevice("ESP32-IoT-Device-003", "AA:BB:CC:DD:EE:03", -78)
    )

    private fun createMockDevice(name: String, address: String, rssi: Int): BleScannedDevice {
        return BleScannedDevice(
            device = null as BluetoothDevice?,
            name = name,
            address = address,
            rssi = rssi
        )
    }

    fun startScan() {
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
                !hasToken -> {
                    _connectionState.update {
                        it.copy(setupState = BleSetupState.TOKEN_REQUESTED, message = "Device needs token")
                    }
                    _deviceRequest.emit(BleRequestCode.REQUEST_TOKEN)
                }
                !hasThreshold -> {
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
        _connectionState.update { it.copy(setupState = BleSetupState.TOKEN_SENDING) }

        scope.launch {
            delay(1000)
            hasToken = true
            _connectionState.update { it.copy(setupState = BleSetupState.TOKEN_CONFIRMED, message = "Token saved successfully") }
            _deviceResponse.emit(BleResponseCode.TOKEN_OK to "Token saved successfully")

            delay(1000)
            if (!hasThreshold) {
                _connectionState.update {
                    it.copy(setupState = BleSetupState.THRESHOLD_REQUESTED, message = "Device needs threshold settings")
                }
                _deviceRequest.emit(BleRequestCode.REQUEST_THRESHOLD)
            }
        }
    }

    fun sendThresholds(thresholds: ThresholdSettings) {
        _connectionState.update { it.copy(setupState = BleSetupState.THRESHOLD_SENDING) }

        scope.launch {
            delay(1000)
            hasThreshold = true
            _connectionState.update { it.copy(setupState = BleSetupState.THRESHOLD_CONFIRMED, message = "Thresholds saved successfully") }
            _deviceResponse.emit(BleResponseCode.THRESHOLD_OK to "Thresholds saved successfully")

            delay(500)
            _connectionState.update { it.copy(setupState = BleSetupState.DEVICE_READY, message = "Device is ready") }
            _deviceRequest.emit(BleRequestCode.DEVICE_READY)
        }
    }

    fun disconnect() {
        _connectionState.update { BleManager.ConnectionState() }
    }

    fun isBluetoothEnabled(): Boolean = true

    fun resetMockState() {
        hasToken = false
        hasThreshold = false
        _scanState.update { BleManager.ScanState() }
        _connectionState.update { BleManager.ConnectionState() }
    }
}
