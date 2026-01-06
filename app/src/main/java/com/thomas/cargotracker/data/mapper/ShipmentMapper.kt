package com.thomas.cargotracker.data.mapper

import com.thomas.cargotracker.domain.model.SensorData
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.dto.LocationResponse
import com.thomas.cargotracker.dto.ShipmentResponse
import com.thomas.cargotracker.dto.TelemetryResponse

object ShipmentMapper {

    fun mapToDomain(
        dto: ShipmentResponse, 
        customerName: String = "Unknown",
        sensorData: SensorData? = null
    ): Shipment {
        return Shipment(
            id = dto.id,
            trackingId = dto.id.take(8).uppercase(), // Simulating a short tracking ID
            status = dto.status,
            description = dto.goodsDescription,
            origin = dto.pickupAddress,
            destination = dto.deliveryAddress,
            senderId = dto.providerId,
            receiverId = dto.customerId,
            shipperId = dto.shipperId,
            deviceId = dto.deviceId,
            customerName = customerName,
            createdDate = dto.createdAt ?: "", // Should format date if needed
            sensorData = sensorData
        )
    }

    fun mapSensorData(telemetry: TelemetryResponse?, location: LocationResponse?): SensorData? {
        if (telemetry == null && location == null) return null
        
        return SensorData(
            temperature = telemetry?.temperature,
            humidity = telemetry?.humidity,
            pressure = telemetry?.pressure,
            latitude = location?.latitude,
            longitude = location?.longitude,
            batteryLevel = telemetry?.batteryLevel,
            signalStrength = telemetry?.signalStrength,
            isMoving = telemetry?.isMoving,
            lastUpdated = telemetry?.time ?: location?.time
        )
    }
}
