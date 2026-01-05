package com.thomas.cargotracker.data.model

data class OrderSummary(
    val id: String,
    val customerName: String,
    val productType: String,
    val status: String = "In Transit",
    val createdDate: Long = System.currentTimeMillis(),
    
    // Sensor Data (Mocked for now)
    val light: String = "Normal",
    val humidity: String = "55%",
    val temperature: String = "24°C",
    val lean: String = "0°",
    
    // Thresholds (for detail view)
    val tempMin: String? = null,
    val tempMax: String? = null,
    val humidityMin: String? = null,
    val humidityMax: String? = null
)
