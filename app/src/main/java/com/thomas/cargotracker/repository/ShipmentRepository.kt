package com.thomas.cargotracker.repository

import com.google.gson.Gson
import com.thomas.cargotracker.dto.AssignDeviceRequest
import com.thomas.cargotracker.dto.AssignShipperRequest
import com.thomas.cargotracker.dto.CancelShipmentRequest
import com.thomas.cargotracker.dto.CompleteShipmentRequest
import com.thomas.cargotracker.dto.CreateShipmentRequest
import com.thomas.cargotracker.dto.ShipmentFilterRequest
import com.thomas.cargotracker.dto.ShipmentListResponse
import com.thomas.cargotracker.dto.ShipmentResponse
import com.thomas.cargotracker.dto.ShipmentStatus
import com.thomas.cargotracker.dto.UpdateShipmentRequest
import com.thomas.cargotracker.network.ApiInterface
import com.thomas.cargotracker.network.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShipmentRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    gson: Gson
): BaseRepository(gson) {
    suspend fun createShipment(
        providerId: String,
        goodsDescription: String,
        pickupAddress: String,
        deliveryAddress: String,
        estimatedDeliveryAt: String? = null
    ): Result<ShipmentResponse> {
        return safeApiCall {
            apiInterface.createShipment(
                CreateShipmentRequest(
                    providerId = providerId,
                    goodsDescription = goodsDescription,
                    pickupAddress = pickupAddress,
                    deliveryAddress = deliveryAddress,
                    estimatedDeliveryAt = estimatedDeliveryAt
                )
            )
        }
    }

    suspend fun getShipmentById(id: String): Result<ShipmentResponse> {
        return safeApiCall {
            apiInterface.getShipmentById(id)
        }
    }

    suspend fun getAllShipments(): Result<List<ShipmentResponse>> {
        return safeApiCall {
            apiInterface.getAllShipments()
        }
    }

    suspend fun getShipmentsByStatus(status: ShipmentStatus): Result<List<ShipmentResponse>> {
        return safeApiCall {
            apiInterface.getShipmentsByStatus(status)
        }
    }

    suspend fun updateShipment(
        id: String,
        goodsDescription: String? = null,
        pickupAddress: String? = null,
        deliveryAddress: String? = null,
        estimatedDeliveryAt: String? = null
    ): Result<ShipmentResponse> {
        return safeApiCall {
            apiInterface.updateShipment(
                id = id,
                request = UpdateShipmentRequest(
                    goodsDescription = goodsDescription,
                    pickupAddress = pickupAddress,
                    deliveryAddress = deliveryAddress,
                    estimatedDeliveryAt = estimatedDeliveryAt
                )
            )
        }
    }

    suspend fun assignShipper(
        shipmentId: String,
        shipperId: String
    ): Result<ShipmentResponse> {
        return safeApiCall {
            apiInterface.assignShipper(
                id = shipmentId,
                request = AssignShipperRequest(shipperId = shipperId)
            )
        }
    }

    suspend fun assignDevice(
        shipmentId: String,
        deviceId: String
    ): Result<ShipmentResponse> {
        return safeApiCall {
            apiInterface.assignDevice(
                id = shipmentId,
                request = AssignDeviceRequest(deviceId = deviceId)
            )
        }
    }

    suspend fun startTransit(shipmentId: String): Result<ShipmentResponse> {
        return safeApiCall {
            apiInterface.startTransit(shipmentId)
        }
    }

    suspend fun completeShipment(
        shipmentId: String,
        deliveredAt: String? = null
    ): Result<ShipmentResponse> {
        return safeApiCall {
            apiInterface.completeShipment(
                id = shipmentId,
                request = CompleteShipmentRequest(deliveredAt = deliveredAt)
            )
        }
    }

    suspend fun cancelShipment(
        shipmentId: String,
        reason: String
    ): Result<ShipmentResponse> {
        return safeApiCall {
            apiInterface.cancelShipment(
                id = shipmentId,
                request = CancelShipmentRequest(reason = reason)
            )
        }
    }

    suspend fun filterShipments(
        status: ShipmentStatus? = null,
        customerId: String? = null,
        providerId: String? = null,
        shipperId: String? = null,
        deviceId: String? = null,
        createdAfter: String? = null,
        createdBefore: String? = null,
        deliveryAfter: String? = null,
        deliveryBefore: String? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
        sortBy: String? = "createdAt",
        sortOrder: String? = "desc"
    ): Result<ShipmentListResponse> {
        return safeApiCall {
            apiInterface.filterShipments(
                ShipmentFilterRequest(
                    status = status,
                    customerId = customerId,
                    providerId = providerId,
                    shipperId = shipperId,
                    deviceId = deviceId,
                    createdAfter = createdAfter,
                    createdBefore = createdBefore,
                    deliveryAfter = deliveryAfter,
                    deliveryBefore = deliveryBefore,
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
