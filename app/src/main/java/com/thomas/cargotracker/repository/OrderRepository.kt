package com.thomas.cargotracker.repository

import com.thomas.cargotracker.dto.*
import com.thomas.cargotracker.network.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface OrderRepository {
    val orders: StateFlow<List<OrderResponse>>
    val pendingOrders: StateFlow<List<OrderResponse>>

    suspend fun fetchOrders()
    suspend fun fetchPendingOrders()
    suspend fun getOrder(id: String): OrderResponse?
    suspend fun createOrder(
        providerId: String,
        goodsDescription: String,
        pickupAddress: String,
        deliveryAddress: String,
        estimatedDeliveryAt: String? = null,
        requireTemperatureTracking: Boolean = false,
        minTemperature: Double? = null,
        maxTemperature: Double? = null,
        requireHumidityTracking: Boolean = false,
        minHumidity: Double? = null,
        maxHumidity: Double? = null,
        requireLocationTracking: Boolean = true,
        specialRequirements: String? = null
    ): OrderResponse?

    suspend fun acceptOrder(orderId: String, notes: String? = null): OrderResponse?
    suspend fun rejectOrder(orderId: String, reason: String): OrderResponse?
    suspend fun filterOrders(status: OrderStatus? = null, search: String? = null)
}

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val api: ApiInterface
) : OrderRepository {

    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    override val orders: StateFlow<List<OrderResponse>> = _orders

    private val _pendingOrders = MutableStateFlow<List<OrderResponse>>(emptyList())
    override val pendingOrders: StateFlow<List<OrderResponse>> = _pendingOrders

    override suspend fun fetchOrders() {
        withContext(Dispatchers.IO) {
            try {
                val response = api.getAllOrders()
                if (response.isSuccessful && response.body() != null) {
                    _orders.value = response.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun fetchPendingOrders() {
        withContext(Dispatchers.IO) {
            try {
                val response = api.getPendingOrders()
                if (response.isSuccessful && response.body() != null) {
                    _pendingOrders.value = response.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun getOrder(id: String): OrderResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getOrderById(id)
                if (response.isSuccessful) response.body() else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun createOrder(
        providerId: String,
        goodsDescription: String,
        pickupAddress: String,
        deliveryAddress: String,
        estimatedDeliveryAt: String?,
        requireTemperatureTracking: Boolean,
        minTemperature: Double?,
        maxTemperature: Double?,
        requireHumidityTracking: Boolean,
        minHumidity: Double?,
        maxHumidity: Double?,
        requireLocationTracking: Boolean,
        specialRequirements: String?
    ): OrderResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateOrderRequest(
                    providerId = providerId,
                    goodsDescription = goodsDescription,
                    pickupAddress = pickupAddress,
                    deliveryAddress = deliveryAddress,
                    estimatedDeliveryAt = estimatedDeliveryAt,
                    requireTemperatureTracking = requireTemperatureTracking,
                    minTemperature = minTemperature,
                    maxTemperature = maxTemperature,
                    requireHumidityTracking = requireHumidityTracking,
                    minHumidity = minHumidity,
                    maxHumidity = maxHumidity,
                    requireLocationTracking = requireLocationTracking,
                    specialRequirements = specialRequirements
                )
                val response = api.createOrder(request)
                if (response.isSuccessful && response.body() != null) {
                    val newOrder = response.body()!!
                    _orders.value += newOrder
                    newOrder
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun acceptOrder(orderId: String, notes: String?): OrderResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.acceptOrder(orderId, AcceptOrderRequest(notes))
                if (response.isSuccessful && response.body() != null) {
                    val updated = response.body()!!
                    updateLocalOrder(updated)
                    // Remove from pending
                    _pendingOrders.value = _pendingOrders.value.filter { it.id != orderId }
                    updated
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun rejectOrder(orderId: String, reason: String): OrderResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.rejectOrder(orderId, RejectOrderRequest(reason))
                if (response.isSuccessful && response.body() != null) {
                    val updated = response.body()!!
                    updateLocalOrder(updated)
                    // Remove from pending
                    _pendingOrders.value = _pendingOrders.value.filter { it.id != orderId }
                    updated
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun filterOrders(status: OrderStatus?, search: String?) {
        withContext(Dispatchers.IO) {
            try {
                val request = OrderFilterRequest(
                    status = status,
                    search = search
                )
                val response = api.filterOrders(request)
                if (response.isSuccessful && response.body() != null) {
                    _orders.value = response.body()!!.orders
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateLocalOrder(updated: OrderResponse) {
        val currentList = _orders.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            currentList[index] = updated
            _orders.value = currentList
        } else {
            _orders.value = currentList + updated
        }
    }
}