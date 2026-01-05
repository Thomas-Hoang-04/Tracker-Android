package com.thomas.cargotracker.dto

enum class BleRequestCode(val code: Int) {
    REQUEST_TOKEN(1),
    REQUEST_THRESHOLD(2),
    DEVICE_READY(3);

    companion object {
        fun fromCode(code: Int): BleRequestCode? = entries.find { it.code == code }
    }
}

enum class BleResponseCode(val code: Int) {
    TOKEN_OK(10),
    TOKEN_ERROR(11),
    THRESHOLD_OK(20),
    THRESHOLD_ERROR(21);

    companion object {
        fun fromCode(code: Int): BleResponseCode? = entries.find { it.code == code }
    }
}

data class BleDeviceRequest(
    val requestCode: Int,
    val message: String? = null
) {
    fun toRequestCode(): BleRequestCode? = BleRequestCode.fromCode(requestCode)
}

data class BleDeviceResponse(
    val responseCode: Int,
    val message: String? = null,
    val timestamp: Long? = null
) {
    fun toResponseCode(): BleResponseCode? = BleResponseCode.fromCode(responseCode)
}

data class ThresholdSettings(
    val tempMin: Float = 20.0f,
    val tempMax: Float = 30.0f,
    val humidityMin: Float = 30.0f,
    val humidityMax: Float = 60.0f,
    val gasThreshold: Int = 100,
    val accelThreshold: Float = 1.0f,
    val gyroThreshold: Float = 1.0f,
    val readPeriodMs: Long = 30000L
) {
    fun toJsonString(): String {
        return """{"temp_min":$tempMin,"temp_max":$tempMax,"humidity_min":$humidityMin,"humidity_max":$humidityMax,"gas_threshold":$gasThreshold,"accel_threshold":$accelThreshold,"gyro_threshold":$gyroThreshold,"read_period_ms":$readPeriodMs}"""
    }
}

data class TokenData(
    val token: String
) {
    fun toJsonString(): String = """{"token":"$token"}"""
}
