package com.thomas.cargotracker.dto

data class CreateDeviceRequest(
    val hardwareUID: String,
    val deviceName: String? = null,
    val model: String? = null,
    val firmwareVersion: String? = null
)

data class DeviceFilterRequest(
    val status: DeviceStatus? = null,
    val providerId: String? = null,
    val minBattery: Int? = null,
    val maxBattery: Int? = null,
    val isOffline: Boolean? = null,
    val search: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc"
)

data class UpdateDeviceRequest(
    val deviceName: String? = null,
    val model: String? = null,
    val firmwareVersion: String? = null,
    val batteryLevel: Int? = null
)

data class UpdateStatusRequest(
    val status: DeviceStatus,
    val shipmentId: String? = null
)

data class DeviceResponse(
    val id: String,
    val hardwareUid: String,
    val deviceName: String?,
    val model: String?,
    val providerId: String,
    val currentShipmentId: String?,
    val status: DeviceStatus,
    val firmwareVersion: String?,
    val batteryLevel: Int?,
    val totalTrips: Int,
    val lastSeenAt: String?,
    val isOnline: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)

data class DeviceListResponse(
    val devices: List<DeviceResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
