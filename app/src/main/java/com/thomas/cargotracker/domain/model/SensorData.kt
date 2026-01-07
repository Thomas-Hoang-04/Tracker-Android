package com.thomas.cargotracker.domain.model

data class SensorData(
    val temperature: Double? = null,
    val humidity: Double? = null,
    val co2: Double? = null,
    val light: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val speed: Double? = null,
    val accuracy: Double? = null,
    val lean: Double? = null,
    val batteryLevel: Int? = null,
    val signalStrength: Int? = null,
    val isMoving: Boolean? = null,
    val lastUpdated: String? = null // For UI display
)
