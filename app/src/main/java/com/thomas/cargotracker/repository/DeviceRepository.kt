package com.thomas.cargotracker.repository

import com.google.gson.Gson
import com.thomas.cargotracker.dto.CreateDeviceRequest
import com.thomas.cargotracker.dto.DeviceFilterRequest
import com.thomas.cargotracker.dto.DeviceListResponse
import com.thomas.cargotracker.dto.DeviceResponse
import com.thomas.cargotracker.dto.DeviceStatus
import com.thomas.cargotracker.dto.UpdateDeviceRequest
import com.thomas.cargotracker.dto.UpdateStatusRequest
import com.thomas.cargotracker.network.ApiInterface
import com.thomas.cargotracker.network.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    gson: Gson
): BaseRepository(gson) {
    suspend fun createDevice(
        hardwareUID: String,
        deviceName: String? = null,
        model: String? = null,
        firmwareVersion: String? = null
    ): Result<DeviceResponse> {
        return safeApiCall {
            apiInterface.createDevice(
                CreateDeviceRequest(
                    hardwareUID = hardwareUID,
                    deviceName = deviceName,
                    model = model,
                    firmwareVersion = firmwareVersion
                )
            )
        }
    }

    suspend fun getDeviceById(id: String): Result<DeviceResponse> {
        return safeApiCall {
            apiInterface.getDeviceById(id)
        }
    }

    suspend fun getAllDevices(): Result<List<DeviceResponse>> {
        return safeApiCall {
            apiInterface.getAllDevices()
        }
    }

    suspend fun getDevicesByStatus(status: DeviceStatus): Result<List<DeviceResponse>> {
        return safeApiCall {
            apiInterface.getDevicesByStatus(status)
        }
    }

    suspend fun getDeviceByHardwareUid(hardwareUid: String): Result<DeviceResponse> {
        return safeApiCall {
            apiInterface.getDeviceByHardwareUid(hardwareUid)
        }
    }

    suspend fun getOfflineDevices(thresholdMillis: Long = 300000): Result<List<DeviceResponse>> {
        return safeApiCall {
            apiInterface.getOfflineDevices(thresholdMillis)
        }
    }

    suspend fun getOnlineDevices(thresholdMillis: Long = 300000): Result<List<DeviceResponse>> {
        return safeApiCall {
            apiInterface.getOnlineDevices(thresholdMillis)
        }
    }

    suspend fun getDevicesByShipmentId(shipmentId: String): Result<List<DeviceResponse>> {
        return safeApiCall {
            apiInterface.getDevicesByShipmentId(shipmentId)
        }
    }

    suspend fun updateDevice(
        id: String,
        deviceName: String? = null,
        model: String? = null,
        firmwareVersion: String? = null,
        batteryLevel: Int? = null
    ): Result<DeviceResponse> {
        return safeApiCall {
            apiInterface.updateDevice(
                id = id,
                request = UpdateDeviceRequest(
                    deviceName = deviceName,
                    model = model,
                    firmwareVersion = firmwareVersion,
                    batteryLevel = batteryLevel
                )
            )
        }
    }

    suspend fun updateDeviceStatus(
        id: String,
        status: DeviceStatus,
        shipmentId: String? = null
    ): Result<DeviceResponse> {
        return safeApiCall {
            apiInterface.updateDeviceStatus(
                id = id,
                request = UpdateStatusRequest(
                    status = status,
                    shipmentId = shipmentId
                )
            )
        }
    }

    suspend fun deleteDevice(id: String): Result<Unit> {
        return safeApiCall {
            apiInterface.deleteDevice(id)
        }
    }

    suspend fun assignDeviceToShipment(
        deviceId: String,
        shipmentId: String
    ): Result<DeviceResponse> {
        return safeApiCall {
            apiInterface.assignDeviceToShipment(deviceId, shipmentId)
        }
    }

    suspend fun releaseDeviceFromShipment(deviceId: String): Result<DeviceResponse> {
        return safeApiCall {
            apiInterface.releaseDeviceFromShipment(deviceId)
        }
    }

    suspend fun filterDevices(
        status: DeviceStatus? = null,
        providerId: String? = null,
        minBattery: Int? = null,
        maxBattery: Int? = null,
        isOffline: Boolean? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
        sortBy: String = "createdAt",
        sortOrder: String = "desc"
    ): Result<DeviceListResponse> {
        return safeApiCall {
            apiInterface.filterDevices(
                DeviceFilterRequest(
                    status = status,
                    providerId = providerId,
                    minBattery = minBattery,
                    maxBattery = maxBattery,
                    isOffline = isOffline,
                    search = search,
                    page = page,
                    pageSize = pageSize,
                    sortBy = sortBy,
                    sortOrder = sortOrder
                )
            )
        }
    }
}
