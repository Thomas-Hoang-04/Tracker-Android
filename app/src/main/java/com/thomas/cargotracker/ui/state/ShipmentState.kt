package com.thomas.cargotracker.ui.state

import com.thomas.cargotracker.dto.ShipmentResponse
import com.thomas.cargotracker.dto.ShipmentStatus

data class ShipmentListState(
    val shipments: List<ShipmentResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false
)

data class ShipmentDetailState(
    val shipment: ShipmentResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CreateShipmentState(
    val providerId: String = "",
    val goodsDescription: String = "",
    val pickupAddress: String = "",
    val deliveryAddress: String = "",
    val estimatedDeliveryAt: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val createdShipment: ShipmentResponse? = null,
    val error: String? = null
)

data class ShipmentFilterState(
    val status: ShipmentStatus? = null,
    val search: String = "",
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc"
)

data class ShipmentActionState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

