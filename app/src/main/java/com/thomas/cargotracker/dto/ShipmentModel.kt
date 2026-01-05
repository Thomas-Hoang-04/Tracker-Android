package com.thomas.cargotracker.dto

data class CreateShipmentRequest(
    val providerId: String,
    val goodsDescription: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val estimatedDeliveryAt: String? = null
)

data class UpdateShipmentRequest(
    val goodsDescription: String? = null,
    val pickupAddress: String? = null,
    val deliveryAddress: String? = null,
    val estimatedDeliveryAt: String? = null
)

data class CompleteShipmentRequest(
    val deliveredAt: String? = null
)

data class AssignDeviceRequest(
    val deviceId: String
)

data class AssignShipperRequest(
    val shipperId: String
)

data class CancelShipmentRequest(
    val reason: String
)

data class ShipmentFilterRequest(
    val status: ShipmentStatus? = null,
    val customerId: String? = null,
    val providerId: String? = null,
    val shipperId: String? = null,
    val deviceId: String? = null,
    val createdAfter: String? = null,
    val createdBefore: String? = null,
    val deliveryAfter: String? = null,
    val deliveryBefore: String? = null,
    val search: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: String? = "createdAt",
    val sortOrder: String? = "desc"
)

data class ShipmentResponse(
    val id: String,
    val status: ShipmentStatus,
    val customerId: String,
    val providerId: String,
    val shipperId: String?,
    val deviceId: String?,
    val goodsDescription: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val estimatedDeliveryAt: String?,
    val actualDeliveryAt: String?,
    val isDelayed: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)

data class ShipmentListResponse(
    val shipments: List<ShipmentResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
