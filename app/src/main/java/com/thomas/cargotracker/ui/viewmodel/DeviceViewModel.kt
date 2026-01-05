package com.thomas.cargotracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.dto.DeviceResponse
import com.thomas.cargotracker.dto.DeviceStatus
import com.thomas.cargotracker.network.Result
import com.thomas.cargotracker.repository.DeviceRepository
import com.thomas.cargotracker.ui.state.CreateDeviceState
import com.thomas.cargotracker.ui.state.DeviceActionState
import com.thomas.cargotracker.ui.state.DeviceDetailState
import com.thomas.cargotracker.ui.state.DeviceFilterState
import com.thomas.cargotracker.ui.state.DeviceListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(DeviceListState())
    val listState: StateFlow<DeviceListState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(DeviceDetailState())
    val detailState: StateFlow<DeviceDetailState> = _detailState.asStateFlow()

    private val _createState = MutableStateFlow(CreateDeviceState())
    val createState: StateFlow<CreateDeviceState> = _createState.asStateFlow()

    private val _filterState = MutableStateFlow(DeviceFilterState())
    val filterState: StateFlow<DeviceFilterState> = _filterState.asStateFlow()

    private val _actionState = MutableStateFlow(DeviceActionState())
    val actionState: StateFlow<DeviceActionState> = _actionState.asStateFlow()

    private val pageSize = 20

    fun loadDevices(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _listState.update { it.copy(isRefreshing = true, currentPage = 1) }
            } else {
                _listState.update { it.copy(isLoading = true) }
            }

            val filter = _filterState.value
            when (val result = deviceRepository.filterDevices(
                status = filter.status,
                search = filter.search.ifBlank { null },
                minBattery = filter.minBattery,
                maxBattery = filter.maxBattery,
                isOffline = filter.isOffline,
                page = if (refresh) 1 else _listState.value.currentPage,
                pageSize = pageSize,
                sortBy = filter.sortBy,
                sortOrder = filter.sortOrder
            )) {
                is Result.Success -> {
                    val data = result.data
                    _listState.update {
                        it.copy(
                            devices = if (refresh) data.devices else it.devices + data.devices,
                            isLoading = false,
                            isRefreshing = false,
                            currentPage = data.page,
                            totalPages = data.totalPages,
                            hasMore = data.page < data.totalPages,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _listState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun loadMoreDevices() {
        val state = _listState.value
        if (state.isLoading || !state.hasMore) return

        _listState.update { it.copy(currentPage = it.currentPage + 1) }
        loadDevices()
    }

    fun refreshDevices() {
        loadDevices(refresh = true)
    }

    fun loadDeviceById(id: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            when (val result = deviceRepository.getDeviceById(id)) {
                is Result.Success -> {
                    _detailState.update {
                        it.copy(isLoading = false, device = result.data, error = null)
                    }
                }
                is Result.Error -> {
                    _detailState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun loadOnlineDevices() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, error = null) }
            when (val result = deviceRepository.getOnlineDevices()) {
                is Result.Success -> {
                    _listState.update {
                        it.copy(
                            devices = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _listState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun loadOfflineDevices() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, error = null) }
            when (val result = deviceRepository.getOfflineDevices()) {
                is Result.Success -> {
                    _listState.update {
                        it.copy(
                            devices = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _listState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    // Filter
    fun updateStatusFilter(status: DeviceStatus?) {
        _filterState.update { it.copy(status = status) }
        loadDevices(refresh = true)
    }

    fun updateSearchFilter(search: String) {
        _filterState.update { it.copy(search = search) }
    }

    fun updateBatteryFilter(min: Int?, max: Int?) {
        _filterState.update { it.copy(minBattery = min, maxBattery = max) }
        loadDevices(refresh = true)
    }

    fun updateOfflineFilter(isOffline: Boolean?) {
        _filterState.update { it.copy(isOffline = isOffline) }
        loadDevices(refresh = true)
    }

    fun applySearch() {
        loadDevices(refresh = true)
    }

    fun clearFilters() {
        _filterState.value = DeviceFilterState()
        loadDevices(refresh = true)
    }

    // Create Device
    fun updateHardwareUID(uid: String) {
        _createState.update { it.copy(hardwareUID = uid, error = null) }
    }

    fun updateDeviceName(name: String) {
        _createState.update { it.copy(deviceName = name, error = null) }
    }

    fun updateModel(model: String) {
        _createState.update { it.copy(model = model, error = null) }
    }

    fun updateFirmwareVersion(version: String) {
        _createState.update { it.copy(firmwareVersion = version, error = null) }
    }

    fun createDevice() {
        val state = _createState.value
        if (state.hardwareUID.isBlank()) {
            _createState.update { it.copy(error = "Hardware UID is required") }
            return
        }

        viewModelScope.launch {
            _createState.update { it.copy(isLoading = true, error = null) }
            when (val result = deviceRepository.createDevice(
                hardwareUID = state.hardwareUID,
                deviceName = state.deviceName.ifBlank { null },
                model = state.model.ifBlank { null },
                firmwareVersion = state.firmwareVersion.ifBlank { null }
            )) {
                is Result.Success -> {
                    _createState.update {
                        it.copy(isLoading = false, isSuccess = true, createdDevice = result.data)
                    }
                    refreshDevices()
                }
                is Result.Error -> {
                    _createState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetCreateState() {
        _createState.value = CreateDeviceState()
    }

    // Actions
    fun updateDeviceStatus(deviceId: String, status: DeviceStatus, shipmentId: String? = null) {
        performAction {
            deviceRepository.updateDeviceStatus(deviceId, status, shipmentId)
        }
    }

    fun assignToShipment(deviceId: String, shipmentId: String) {
        performAction {
            deviceRepository.assignDeviceToShipment(deviceId, shipmentId)
        }
    }

    fun releaseFromShipment(deviceId: String) {
        performAction {
            deviceRepository.releaseDeviceFromShipment(deviceId)
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            _actionState.update { it.copy(isLoading = true, error = null) }
            when (val result = deviceRepository.deleteDevice(deviceId)) {
                is Result.Success -> {
                    _actionState.update { it.copy(isLoading = false, isSuccess = true) }
                    refreshDevices()
                }
                is Result.Error -> {
                    _actionState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun performAction(action: suspend () -> Result<DeviceResponse>) {
        viewModelScope.launch {
            _actionState.update { it.copy(isLoading = true, error = null) }
            when (val result = action()) {
                is Result.Success -> {
                    _actionState.update { it.copy(isLoading = false, isSuccess = true) }
                    _detailState.update { it.copy(device = result.data) }
                    refreshDevices()
                }
                is Result.Error -> {
                    _actionState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetActionState() {
        _actionState.value = DeviceActionState()
    }

    fun clearListError() {
        _listState.update { it.copy(error = null) }
    }

    fun clearDetailError() {
        _detailState.update { it.copy(error = null) }
    }
}
