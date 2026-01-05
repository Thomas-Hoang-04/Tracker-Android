package com.thomas.cargotracker.ui.viewmodel

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleViewModel @Inject constructor(
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
                                successMessage = "Device provisioned successfully!"
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
                                successMessage = message ?: "Thresholds saved"
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
        if (useMock) mockBleManager.startScan() else bleManager.startScan()
    }

    fun stopScan() {
        if (useMock) mockBleManager.stopScan() else bleManager.stopScan()
    }

    fun connectToDevice(device: BleScannedDevice) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        if (useMock) mockBleManager.connect(device) else bleManager.connect(device)
        startConnectionTimeout()
    }

    fun sendToken(token: String) {
        if (token.isBlank()) {
            _uiState.update { it.copy(error = "Token cannot be empty") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        if (useMock) mockBleManager.sendToken(token) else bleManager.sendToken(token)
        startOperationTimeout()
    }

    fun sendThresholds(thresholds: ThresholdSettings) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        if (useMock) mockBleManager.sendThresholds(thresholds) else bleManager.sendThresholds(thresholds)
        startOperationTimeout()
    }

    fun disconnect() {
        cancelTimeout()
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
    val setupComplete: Boolean = false
)
