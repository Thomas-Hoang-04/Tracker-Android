package com.thomas.cargotracker.ui.state

import com.thomas.cargotracker.dto.DeviceResponse
import com.thomas.cargotracker.dto.DeviceStatus

data class DeviceListState(
    val devices: List<DeviceResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false
)

data class DeviceDetailState(
    val device: DeviceResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CreateDeviceState(
    val hardwareUID: String = "",
    val deviceName: String = "",
    val model: String = "",
    val firmwareVersion: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val createdDevice: DeviceResponse? = null,
    val error: String? = null
)

data class DeviceFilterState(
    val status: DeviceStatus? = null,
    val search: String = "",
    val minBattery: Int? = null,
    val maxBattery: Int? = null,
    val isOffline: Boolean? = null,
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc"
)

data class DeviceActionState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

