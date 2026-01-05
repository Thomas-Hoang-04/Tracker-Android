package com.thomas.cargotracker.ble

import java.util.UUID

object BleConstants {
    const val DEVICE_NAME_PREFIX = "ESP32-IoT-Device"

    val SERVICE_UUID: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
    val WRITE_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    val NOTIFY_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    const val SCAN_TIMEOUT_MS = 10000L
    const val OPERATION_TIMEOUT_MS = 5000L
    const val CONNECTION_TIMEOUT_MS = 10000L
}
