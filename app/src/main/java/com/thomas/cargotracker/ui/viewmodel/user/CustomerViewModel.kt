package com.thomas.cargotracker.ui.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.dto.OrderResponse
import com.thomas.cargotracker.dto.OrderStatus
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.network.Result
import com.thomas.cargotracker.repository.OrderRepository
import com.thomas.cargotracker.repository.ShipmentRepository
import com.thomas.cargotracker.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val shipmentRepository: ShipmentRepository
) : ViewModel() {

    val orders: StateFlow<List<OrderResponse>> = orderRepository.orders
    private val _shipments = MutableStateFlow<List<Shipment>>(emptyList())
    val shipments: StateFlow<List<Shipment>> = _shipments.asStateFlow()

    data class UserResult(val name: String, val id: String, val role: String, val address: String?)
    private val _users = MutableStateFlow<List<UserResult>>(emptyList())
    val users: StateFlow<List<UserResult>> = _users.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            fetchOrders()
            fetchShipments()
            fetchUsers()
        }
    }

    private suspend fun fetchOrders() = orderRepository.fetchOrders()

    private suspend fun fetchShipments() {
        val acceptedOrders = orders.value.filter {
            it.status == OrderStatus.ACCEPTED && it.shipmentId != null
        }

        val shipmentList = mutableListOf<Shipment>()
        for (order in acceptedOrders) {
            order.shipmentId?.let { shipmentId ->
                shipmentRepository.getShipment(shipmentId)?.let { shipment ->
                    shipmentList.add(shipment)
                }
            }
        }
        _shipments.value = shipmentList
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                when (val providersResult = userRepository.getUsersByRole(UserRole.PROVIDER)) {
                    is Result.Success -> {
                        _users.value = providersResult.data
                            .filter { it.isActive }
                            .map { user ->
                                UserResult(
                                    name = user.fullName,
                                    id = user.id,
                                    role = user.role.toString(),
                                    address = user.address
                                )
                            }
                    }
                    is Result.Error -> {
                        _users.value = emptyList()
                    }
                    is Result.Loading -> {
                        // Keep current list while loading
                    }
                }
            } catch (_: Exception) {
                _users.value = emptyList()
            }
        }
    }

    fun refreshUsers() {
        fetchUsers()
    }

    sealed class CreateOrderState {
        object Idle : CreateOrderState()
        object Loading : CreateOrderState()
        data class Success(val order: OrderResponse) : CreateOrderState()
        data class Error(val message: String) : CreateOrderState()
    }

    private val _createOrderState = MutableStateFlow<CreateOrderState>(CreateOrderState.Idle)
    val createOrderState: StateFlow<CreateOrderState> = _createOrderState.asStateFlow()

    fun resetCreateOrderState() {
        _createOrderState.value = CreateOrderState.Idle
    }

    fun createOrder(
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
    ) {
        viewModelScope.launch {
            _createOrderState.value = CreateOrderState.Loading
            try {
                val order = orderRepository.createOrder(
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

                if (order != null) {
                    _createOrderState.value = CreateOrderState.Success(order)
                    loadData() // Refresh list
                } else {
                    _createOrderState.value = CreateOrderState.Error("Failed to create order")
                }
            } catch (e: Exception) {
                _createOrderState.value = CreateOrderState.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getOrder(id: String): OrderResponse? {
        return orderRepository.getOrder(id)
    }

    fun refreshOrders() {
        loadData()
    }
}
