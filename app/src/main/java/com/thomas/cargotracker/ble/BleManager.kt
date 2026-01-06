package com.thomas.cargotracker.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.thomas.cargotracker.dto.BleDeviceRequest
import com.thomas.cargotracker.dto.BleDeviceResponse
import com.thomas.cargotracker.dto.BleRequestCode
import com.thomas.cargotracker.dto.BleResponseCode
import com.thomas.cargotracker.dto.ThresholdSettings
import com.thomas.cargotracker.dto.TokenData
import com.thomas.cargotracker.ui.state.BleError
import com.thomas.cargotracker.ui.state.BleScannedDevice
import com.thomas.cargotracker.ui.state.BleSetupState
import dagger.hilt.android.qualifiers.ApplicationContext
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
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    @field:ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _scanState = MutableStateFlow(ScanState())
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _deviceRequest = MutableSharedFlow<BleRequestCode>()
    val deviceRequest = _deviceRequest.asSharedFlow()

    private val _deviceResponse = MutableSharedFlow<Pair<BleResponseCode, String?>>()
    val deviceResponse = _deviceResponse.asSharedFlow()

    private val _error = MutableSharedFlow<BleError>()
    val error = _error.asSharedFlow()

    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null

    private val scannedDevices = mutableMapOf<String, BleScannedDevice>()

    data class ScanState(
        val isScanning: Boolean = false,
        val devices: List<BleScannedDevice> = emptyList()
    )

    data class ConnectionState(
        val setupState: BleSetupState = BleSetupState.DISCONNECTED,
        val connectedDevice: BleScannedDevice? = null,
        val message: String? = null
    )

    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: return
            if (!name.startsWith(BleConstants.DEVICE_NAME_PREFIX)) return

            val bleDevice = BleScannedDevice(
                device = device,
                name = name,
                address = device.address,
                rssi = result.rssi
            )
            scannedDevices[device.address] = bleDevice
            _scanState.update { it.copy(devices = scannedDevices.values.toList()) }
        }

        override fun onScanFailed(errorCode: Int) {
            _scanState.update { it.copy(isScanning = false) }
            scope.launch { _error.emit(BleError.Unknown("Scan failed with code: $errorCode")) }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.update { it.copy(setupState = BleSetupState.CONNECTED) }
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.update { 
                        it.copy(setupState = BleSetupState.DISCONNECTED, connectedDevice = null) 
                    }
                    cleanup()
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                scope.launch { _error.emit(BleError.ServiceDiscoveryFailed) }
                return
            }

            val service = gatt.getService(BleConstants.SERVICE_UUID)
            if (service == null) {
                scope.launch { _error.emit(BleError.ServiceDiscoveryFailed) }
                return
            }

            writeCharacteristic = service.getCharacteristic(BleConstants.WRITE_CHARACTERISTIC_UUID)
            notifyCharacteristic = service.getCharacteristic(BleConstants.NOTIFY_CHARACTERISTIC_UUID)

            notifyCharacteristic?.let { char ->
                gatt.setCharacteristicNotification(char, true)
                val descriptor = char.getDescriptor(BleConstants.CCCD_UUID)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
            }

            _connectionState.update { it.copy(setupState = BleSetupState.WAITING_FOR_REQUEST) }
        }

        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val data = characteristic.value
            handleNotification(data)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleNotification(value)
        }
    }

    private fun handleNotification(data: ByteArray) {
        val jsonString = data.toString(Charsets.UTF_8)
        val json = JSONObject(jsonString)

        when {
            json.has("requestCode") -> {
                val request = BleDeviceRequest(
                    requestCode = json.getInt("requestCode"),
                    message = json.optString("message", "null")
                )
                request.toRequestCode()?.let { code ->
                    val newState = when (code) {
                        BleRequestCode.REQUEST_TOKEN -> BleSetupState.TOKEN_REQUESTED
                        BleRequestCode.REQUEST_THRESHOLD -> BleSetupState.THRESHOLD_REQUESTED
                        BleRequestCode.DEVICE_READY -> BleSetupState.DEVICE_READY
                    }
                    _connectionState.update { it.copy(setupState = newState, message = request.message) }
                    scope.launch { _deviceRequest.emit(code) }
                }
            }
            json.has("responseCode") -> {
                val response = BleDeviceResponse(
                    responseCode = json.getInt("responseCode"),
                    message = json.optString("message", "null"),
                    timestamp = json.optLong("timestamp", 0)
                )
                response.toResponseCode()?.let { code ->
                    val newState = when (code) {
                        BleResponseCode.TOKEN_OK -> BleSetupState.TOKEN_CONFIRMED
                        BleResponseCode.TOKEN_ERROR -> BleSetupState.TOKEN_REQUESTED
                        BleResponseCode.THRESHOLD_OK -> BleSetupState.THRESHOLD_CONFIRMED
                        BleResponseCode.THRESHOLD_ERROR -> BleSetupState.THRESHOLD_REQUESTED
                    }
                    _connectionState.update { it.copy(setupState = newState, message = response.message) }
                    scope.launch { _deviceResponse.emit(code to response.message) }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            scope.launch { _error.emit(BleError.BluetoothDisabled) }
            return
        }

        scannedDevices.clear()
        _scanState.update { ScanState(isScanning = true, devices = emptyList()) }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothAdapter.bluetoothLeScanner?.startScan(null, settings, scanCallback)

        scope.launch {
            delay(BleConstants.SCAN_TIMEOUT_MS)
            stopScan()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        _scanState.update { it.copy(isScanning = false) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun connect(device: BleScannedDevice) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            scope.launch { _error.emit(BleError.BluetoothDisabled) }
            return
        }

        val bluetoothDevice = device.device ?: run {
            scope.launch { _error.emit(BleError.DeviceNotFound) }
            return
        }
        stopScan()
        _connectionState.update { 
            ConnectionState(setupState = BleSetupState.CONNECTING, connectedDevice = device) 
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            scope.launch { _error.emit(BleError.PermissionDenied) }
            _connectionState.update { ConnectionState() }
            return
        }
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendToken(token: String) {
        _connectionState.update { it.copy(setupState = BleSetupState.TOKEN_SENDING) }
        val data = TokenData(token).toJsonString().toByteArray(Charsets.UTF_8)
        writeData(data)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendThresholds(thresholds: ThresholdSettings) {
        _connectionState.update { it.copy(setupState = BleSetupState.THRESHOLD_SENDING) }
        val data = thresholds.toJsonString().toByteArray(Charsets.UTF_8)
        writeData(data)
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Suppress("DEPRECATION")
    private fun writeData(data: ByteArray) {
        val gatt = bluetoothGatt
        val characteristic = writeCharacteristic

        if (gatt == null || characteristic == null) {
            scope.launch { _error.emit(BleError.WriteFailed) }
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        } else {
            characteristic.value = data
            gatt.writeCharacteristic(characteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        bluetoothGatt?.disconnect()
        cleanup()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun cleanup() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        writeCharacteristic = null
        notifyCharacteristic = null
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
}
