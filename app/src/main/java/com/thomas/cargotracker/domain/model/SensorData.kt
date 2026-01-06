package com.thomas.cargotracker.domain.model

data class SensorData(
    val temperature: Double? = null,
    val humidity: Double? = null,
    val pressure: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val batteryLevel: Int? = null,
    val signalStrength: Int? = null,
    val isMoving: Boolean? = null,
    val lastUpdated: String? = null // For UI display
)
