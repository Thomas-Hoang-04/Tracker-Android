package com.thomas.cargotracker.dto

enum class OrderStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class CreateOrderRequest(
    val providerId: String,
    val goodsDescription: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val estimatedDeliveryAt: String? = null,
    val requireTemperatureTracking: Boolean = false,
    val minTemperature: Double? = null,
    val maxTemperature: Double? = null,
    val requireHumidityTracking: Boolean = false,
    val minHumidity: Double? = null,
    val maxHumidity: Double? = null,
    val requireLocationTracking: Boolean = true,
    val specialRequirements: String? = null
)

data class AcceptOrderRequest(
    val notes: String? = null
)

data class RejectOrderRequest(
    val reason: String
)

data class OrderFilterRequest(
    val status: OrderStatus? = null,
    val customerId: String? = null,
    val providerId: String? = null,
    val createdAfter: String? = null,
    val createdBefore: String? = null,
    val search: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: String? = "createdAt",
    val sortOrder: String? = "desc"
)

data class OrderResponse(
    val id: String,
    val customerId: String,
    val providerId: String,
    val status: OrderStatus,
    val goodsDescription: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val estimatedDeliveryAt: String?,
    val requireTemperatureTracking: Boolean,
    val minTemperature: Double?,
    val maxTemperature: Double?,
    val requireHumidityTracking: Boolean,
    val minHumidity: Double?,
    val maxHumidity: Double?,
    val requireLocationTracking: Boolean,
    val specialRequirements: String?,
    val shipmentId: String?,
    val rejectionReason: String?,
    val processedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class OrderListResponse(
    val orders: List<OrderResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)