package com.thomas.cargotracker.dto

data class TelemetryResponse(
    val deviceId: String,
    val time: String,
    val temperature: Double?,
    val humidity: Double?,
    val co2: Double?,
    val light: Double?,
    val latitude: Double?,
    val longitude: Double?,
    val speed: Double?,
    val accuracy: Double?,
    val lean: Double?,
    val batteryLevel: Int?,
    val signalStrength: Int?,
    val isMoving: Boolean?
)

data class LocationResponse(
    val deviceId: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val speed: Double?,
    val heading: Double?,
    val accuracy: Double?
)
