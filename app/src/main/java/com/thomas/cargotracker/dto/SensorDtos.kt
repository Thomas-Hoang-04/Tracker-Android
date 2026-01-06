package com.thomas.cargotracker.dto

import java.util.UUID
import java.time.Instant

data class TelemetryResponse(
    val deviceId: String,
    val time: String,
    val temperature: Double?,
    val humidity: Double?,
    val pressure: Double?,
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
