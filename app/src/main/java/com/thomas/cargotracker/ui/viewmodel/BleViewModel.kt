package com.thomas.cargotracker.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.ble.BleConfig
import com.thomas.cargotracker.ble.BleManager
import com.thomas.cargotracker.ble.MockBleManager
import com.thomas.cargotracker.dto.BleRequestCode
import com.thomas.cargotracker.dto.BleResponseCode
import com.thomas.cargotracker.dto.ThresholdSettings
import com.thomas.cargotracker.ui.state.BleError
import com.thomas.cargotracker.ui.state.BleScannedDevice
import com.thomas.cargotracker.ui.state.BleSetupState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

object TestDeviceData {
    const val DEVICE_ID = "88e4a1fc-2939-4787-bb68-d52b0d7d30cf"
    const val HARDWARE_UID = "49aa75c3-d942-4a13-8a75-34a93a32362e"
    const val PROVIDER_ID = "e811533d-1f5a-4eee-9456-b33d682969d8"
}

@HiltViewModel
class BleViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val bleManager: BleManager,
    private val mockBleManager: MockBleManager
) : ViewModel() {

    private val useMock = BleConfig.USE_MOCK_BLE

    val scanState = if (useMock) mockBleManager.scanState else bleManager.scanState
    val connectionState = if (useMock) mockBleManager.connectionState else bleManager.connectionState

    private val deviceRequestFlow = if (useMock) mockBleManager.deviceRequest else bleManager.deviceRequest
    private val deviceResponseFlow = if (useMock) mockBleManager.deviceResponse else bleManager.deviceResponse
    private val errorFlow = if (useMock) mockBleManager.error else bleManager.error

    private val _uiState = MutableStateFlow(BleUiState())
    val uiState: StateFlow<BleUiState> = _uiState.asStateFlow()

    private var timeoutJob: Job? = null

    init {
        observeDeviceRequests()
        observeDeviceResponses()
        observeErrors()
    }

    private fun observeDeviceRequests() {
        viewModelScope.launch {
            deviceRequestFlow.collect { request ->
                cancelTimeout()
                when (request) {
                    BleRequestCode.REQUEST_TOKEN -> {
                        _uiState.update { it.copy(showTokenInput = true, showThresholdInput = false) }
                    }
                    BleRequestCode.REQUEST_THRESHOLD -> {
                        _uiState.update { it.copy(showTokenInput = false, showThresholdInput = true) }
                    }
                    BleRequestCode.DEVICE_READY -> {
                        _uiState.update { 
                            it.copy(
                                showTokenInput = false, 
                                showThresholdInput = false,
                                setupComplete = true,
                                successMessage = "Device provisioned successfully!",
                                provisionedDeviceId = TestDeviceData.DEVICE_ID
                            ) 
                        }
                    }
                }
            }
        }
    }

    private fun observeDeviceResponses() {
        viewModelScope.launch {
            deviceResponseFlow.collect { (code, message) ->
                cancelTimeout()
                when (code) {
                    BleResponseCode.TOKEN_OK -> {
                        _uiState.update { 
                            it.copy(showTokenInput = false, successMessage = message ?: "Token saved") 
                        }
                    }
                    BleResponseCode.TOKEN_ERROR -> {
                        _uiState.update { 
                            it.copy(error = message ?: "Token save failed", isLoading = false) 
                        }
                    }
                    BleResponseCode.THRESHOLD_OK -> {
                        _uiState.update { 
                            it.copy(
                                showThresholdInput = false, 
                                setupComplete = true,
                                successMessage = message ?: "Thresholds saved",
                                provisionedDeviceId = TestDeviceData.DEVICE_ID
                            ) 
                        }
                    }
                    BleResponseCode.THRESHOLD_ERROR -> {
                        _uiState.update { 
                            it.copy(error = message ?: "Threshold save failed", isLoading = false) 
                        }
                    }
                }
            }
        }
    }

    private fun observeErrors() {
        viewModelScope.launch {
            errorFlow.collect { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
        }
    }

    fun isBluetoothEnabled(): Boolean = if (useMock) mockBleManager.isBluetoothEnabled() else bleManager.isBluetoothEnabled()

    fun startScan() {
        if (!isBluetoothEnabled()) {
            _uiState.update { it.copy(error = BleError.BluetoothDisabled.message) }
            return
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(error = BleError.PermissionDenied.message, isLoading = false) }
            return
        }
        if (useMock) mockBleManager.startScan() else bleManager.startScan()
    }

    fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(error = BleError.PermissionDenied.message, isLoading = false) }
            return
        }
        if (useMock) mockBleManager.stopScan() else bleManager.stopScan()
    }

    fun connectToDevice(device: BleScannedDevice) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(error = BleError.PermissionDenied.message, isLoading = false) }
            return
        }
        if (useMock) mockBleManager.connect(device) else bleManager.connect(device)
        startConnectionTimeout()
    }

    fun sendToken(token: String) {
        if (token.isBlank()) {
            _uiState.update { it.copy(error = "Token cannot be empty") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(error = BleError.PermissionDenied.message, isLoading = false) }
            return
        }
        if (useMock) mockBleManager.sendToken(token) else bleManager.sendToken(token)
        startOperationTimeout()
    }

    fun sendThresholds(thresholds: ThresholdSettings) {
        _uiState.update { it.copy(isLoading = true, error = null, setupComplete = false) }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(error = BleError.PermissionDenied.message, isLoading = false) }
            return
        }
        if (useMock) mockBleManager.sendThresholds(thresholds) else bleManager.sendThresholds(thresholds)
        startOperationTimeout()
    }

    fun disconnect() {
        cancelTimeout()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(error = BleError.PermissionDenied.message, isLoading = false) }
            return
        }
        if (useMock) mockBleManager.disconnect() else bleManager.disconnect()
        resetState()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun resetState() {
        _uiState.update { BleUiState() }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun startConnectionTimeout() {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(10000)
            if (connectionState.value.setupState == BleSetupState.CONNECTING) {
                _uiState.update { it.copy(error = BleError.Timeout.message, isLoading = false) }
                bleManager.disconnect()
            }
        }
    }

    private fun startOperationTimeout() {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(5000)
            val state = connectionState.value.setupState
            if (state == BleSetupState.TOKEN_SENDING || state == BleSetupState.THRESHOLD_SENDING) {
                _uiState.update { it.copy(error = BleError.Timeout.message, isLoading = false) }
            }
        }
    }

    private fun cancelTimeout() {
        timeoutJob?.cancel()
        timeoutJob = null
        _uiState.update { it.copy(isLoading = false) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCleared() {
        super.onCleared()
        bleManager.disconnect()
    }
}

data class BleUiState(
    val showTokenInput: Boolean = false,
    val showThresholdInput: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val setupComplete: Boolean = false,
    val provisionedDeviceId: String? = null
)
