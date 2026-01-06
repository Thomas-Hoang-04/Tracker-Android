package com.thomas.cargotracker.repository

import com.thomas.cargotracker.data.mapper.ShipmentMapper
import com.thomas.cargotracker.domain.model.SensorData
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.data.model.ShipmentStatus
import com.thomas.cargotracker.dto.*
import com.thomas.cargotracker.network.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface ShipmentRepository {
    val shipments: StateFlow<List<Shipment>>
    suspend fun fetchShipments()
    suspend fun getShipment(id: String): Shipment?
    suspend fun createShipment(
        providerId: String,
        goodsDescription: String,
        pickupAddress: String,
        deliveryAddress: String,
        estimatedDeliveryAt: String? = null
    ): Shipment?

    suspend fun filterShipments(
        status: ShipmentStatus? = null,
        search: String? = null
    )

    suspend fun assignShipper(shipmentId: String, shipperId: String): Shipment?
    suspend fun assignDevice(shipmentId: String, deviceId: String): Shipment?
    suspend fun startTransit(shipmentId: String): Shipment?
    suspend fun completeShipment(shipmentId: String, deliveredAt: String?): Shipment?
    suspend fun cancelShipment(shipmentId: String, reason: String): Shipment?
}

@Singleton
class ShipmentRepositoryImpl @Inject constructor(
    private val api: ApiInterface
) : ShipmentRepository {

    private val _shipments = MutableStateFlow<List<Shipment>>(emptyList())
    override val shipments: StateFlow<List<Shipment>> = _shipments

    override suspend fun fetchShipments() {
        withContext(Dispatchers.IO) {
            try {
                val response = api.getAllShipments()
                if (response.isSuccessful && response.body() != null) {
                    val dtoList = response.body()!!
                    // For List View: Map without extra data to avoid N+1
                    val domainList = dtoList.map { ShipmentMapper.mapToDomain(it) }
                    _shipments.value = domainList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun getShipment(id: String): Shipment? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Fetch Shipment Basic Info
                val shipmentResponse = api.getShipmentById(id)
                if (!shipmentResponse.isSuccessful || shipmentResponse.body() == null) {
                    return@withContext null
                }
                val dto = shipmentResponse.body()!!

                // 2. Fetch User Info (Customer Name)
                val userResponse = api.getUserById(dto.customerId)
                val customerName = if (userResponse.isSuccessful && userResponse.body() != null) {
                    userResponse.body()!!.fullName
                } else {
                    "Unknown"
                }

                // 3. Fetch Sensor Data (if device attached)
                var sensorData: SensorData? = null
                if (dto.deviceId != null) {
                    val telemetryDeferred = api.getDeviceTelemetry(dto.deviceId)
                    val locationDeferred = api.getDeviceLocation(dto.deviceId)
                    
                    // Simple sequential fetch for now, could be parallelized with async/await
                    val telemetry = if (telemetryDeferred.isSuccessful) telemetryDeferred.body() else null
                    val location = if (locationDeferred.isSuccessful) locationDeferred.body() else null
                    
                    sensorData = ShipmentMapper.mapSensorData(telemetry, location)
                }

                // 4. Map & Return
                ShipmentMapper.mapToDomain(dto, customerName, sensorData)

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun createShipment(
        providerId: String,
        goodsDescription: String,
        pickupAddress: String,
        deliveryAddress: String,
        estimatedDeliveryAt: String?
    ): Shipment? {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateShipmentRequest(
                    providerId = providerId,
                    goodsDescription = goodsDescription,
                    pickupAddress = pickupAddress,
                    deliveryAddress = deliveryAddress,
                    estimatedDeliveryAt = estimatedDeliveryAt
                )
                val response = api.createShipment(request)
                if (response.isSuccessful && response.body() != null) {
                    val newShipment = ShipmentMapper.mapToDomain(response.body()!!)
                    _shipments.value += newShipment
                    newShipment
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    override suspend fun filterShipments(status: ShipmentStatus?, search: String?) {
        withContext(Dispatchers.IO) {
            try {
                val request = ShipmentFilterRequest(
                    status = status,
                    search = search
                )
                val response = api.filterShipments(request)
                if (response.isSuccessful && response.body() != null) {
                    val dtoList = response.body()!!.shipments
                    val domainList = dtoList.map { ShipmentMapper.mapToDomain(it) }
                    _shipments.value = domainList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun assignShipper(shipmentId: String, shipperId: String): Shipment? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.assignShipper(shipmentId, AssignShipperRequest(shipperId))
                if (response.isSuccessful && response.body() != null) {
                    val updated = ShipmentMapper.mapToDomain(response.body()!!)
                    updateLocalShipment(updated)
                    updated
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun assignDevice(shipmentId: String, deviceId: String): Shipment? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.assignDevice(shipmentId, AssignDeviceRequest(deviceId))
                if (response.isSuccessful && response.body() != null) {
                    val updated = ShipmentMapper.mapToDomain(response.body()!!)
                    updateLocalShipment(updated)
                    updated
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun startTransit(shipmentId: String): Shipment? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.startTransit(shipmentId)
                if (response.isSuccessful && response.body() != null) {
                    val updated = ShipmentMapper.mapToDomain(response.body()!!)
                    updateLocalShipment(updated)
                    updated
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun completeShipment(shipmentId: String, deliveredAt: String?): Shipment? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.completeShipment(shipmentId, CompleteShipmentRequest(deliveredAt))
                if (response.isSuccessful && response.body() != null) {
                    val updated = ShipmentMapper.mapToDomain(response.body()!!)
                    updateLocalShipment(updated)
                    updated
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun cancelShipment(shipmentId: String, reason: String): Shipment? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.cancelShipment(shipmentId, CancelShipmentRequest(reason))
                if (response.isSuccessful && response.body() != null) {
                    val updated = ShipmentMapper.mapToDomain(response.body()!!)
                    updateLocalShipment(updated)
                    updated
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun updateLocalShipment(updated: Shipment) {
        val currentList = _shipments.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            currentList[index] = updated
            _shipments.value = currentList
        } else {
             _shipments.value = currentList + updated
        }
    }
}
