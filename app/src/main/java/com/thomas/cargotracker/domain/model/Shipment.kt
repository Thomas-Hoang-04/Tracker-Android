package com.thomas.cargotracker.domain.model

import com.thomas.cargotracker.data.model.ShipmentStatus

data class Shipment(
    val id: String,
    val trackingId: String, // Shortened ID
    val status: ShipmentStatus,
    val description: String, // Maps to goodsDescription
    val origin: String, // Maps to pickupAddress
    val destination: String, // Maps to deliveryAddress
    
    // IDs
    val senderId: String, // providerId
    val receiverId: String, // customerId
    val shipperId: String? = null,
    val deviceId: String? = null,
    
    // UI Display Fields
    val customerName: String = "Unknown", // Fetched separately
    val providerName: String = "Unknown", // Placeholder if needed
    val createdDate: String = "",
    
    // Live Data
    val sensorData: SensorData? = null
)
