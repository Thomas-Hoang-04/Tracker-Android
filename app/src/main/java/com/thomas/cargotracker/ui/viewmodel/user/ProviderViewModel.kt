package com.thomas.cargotracker.ui.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.data.model.ShipmentStatus
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.dto.OrderResponse
import com.thomas.cargotracker.repository.OrderRepository
import com.thomas.cargotracker.repository.ShipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val shipmentRepository: ShipmentRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    // Shipments for Provider (existing)
    val shipments: StateFlow<List<Shipment>> = shipmentRepository.shipments

    // Pending Orders for Provider approval
    val pendingOrders: StateFlow<List<OrderResponse>> = orderRepository.pendingOrders

    // Filtering State
    data class FilterState(
        val status: ShipmentStatus? = null,
        val search: String = ""
    )
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    init {
        loadShipments()
    }

    fun loadShipments() {
        viewModelScope.launch {
            shipmentRepository.filterShipments(
                status = _filterState.value.status,
                search = _filterState.value.search.ifBlank { null }
            )
        }
    }

    fun loadPendingOrders() {
        viewModelScope.launch {
            orderRepository.fetchPendingOrders()
        }
    }

    fun loadOrders() {
        loadShipments()
        loadPendingOrders()
    }

    fun updateStatusFilter(status: ShipmentStatus?) {
        _filterState.update { it.copy(status = status) }
        loadShipments()
    }

    fun updateSearchFilter(search: String) {
        _filterState.update { it.copy(search = search) }
        loadShipments()
    }
    
    fun applySearch() {
        loadShipments()
    }
    
    fun clearFilters() {
        _filterState.value = FilterState()
        loadShipments()
    }

    // Order Approval State
    sealed class OrderApprovalState {
        object Idle : OrderApprovalState()
        object Loading : OrderApprovalState()
        data class Accepted(val order: OrderResponse) : OrderApprovalState()
        data class Rejected(val order: OrderResponse) : OrderApprovalState()
        data class Error(val message: String) : OrderApprovalState()
    }

    private val _orderApprovalState = MutableStateFlow<OrderApprovalState>(OrderApprovalState.Idle)
    val orderApprovalState: StateFlow<OrderApprovalState> = _orderApprovalState.asStateFlow()

    fun resetOrderApprovalState() {
        _orderApprovalState.value = OrderApprovalState.Idle
    }

    fun acceptOrder(orderId: String, notes: String? = null) {
        viewModelScope.launch {
            _orderApprovalState.value = OrderApprovalState.Loading
            try {
                val order = orderRepository.acceptOrder(orderId, notes)
                if (order != null) {
                    _orderApprovalState.value = OrderApprovalState.Accepted(order)
                    // Refresh both pending orders and shipments
                    loadPendingOrders()
                    loadShipments() // Refresh shipments as new one was created
                } else {
                    _orderApprovalState.value = OrderApprovalState.Error("Failed to accept order")
                }
            } catch (e: Exception) {
                _orderApprovalState.value = OrderApprovalState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun rejectOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            _orderApprovalState.value = OrderApprovalState.Loading
            try {
                val order = orderRepository.rejectOrder(orderId, reason)
                if (order != null) {
                    _orderApprovalState.value = OrderApprovalState.Rejected(order)
                    loadPendingOrders()
                } else {
                    _orderApprovalState.value = OrderApprovalState.Error("Failed to reject order")
                }
            } catch (e: Exception) {
                _orderApprovalState.value = OrderApprovalState.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getOrder(id: String): Shipment? {
        return shipmentRepository.getShipment(id)
    }

    // Shipment Actions
    fun assignShipper(shipmentId: String, shipperId: String) {
        viewModelScope.launch {
            shipmentRepository.assignShipper(shipmentId, shipperId)
            loadShipments()
        }
    }

    fun assignDevice(shipmentId: String, deviceId: String) {
        viewModelScope.launch {
            shipmentRepository.assignDevice(shipmentId, deviceId)
            loadShipments()
        }
    }

    fun startTransit(shipmentId: String) {
        viewModelScope.launch {
            shipmentRepository.startTransit(shipmentId)
            loadShipments()
        }
    }

    fun completeShipment(shipmentId: String, deliveredAt: String? = null) {
        viewModelScope.launch {
            shipmentRepository.completeShipment(shipmentId, deliveredAt)
            loadShipments()
        }
    }

    fun cancelShipment(shipmentId: String, reason: String) {
        viewModelScope.launch {
            shipmentRepository.cancelShipment(shipmentId, reason)
            loadShipments()
        }
    }

    sealed class CreateOrderState {
        object Idle : CreateOrderState()
        object Loading : CreateOrderState()
        data class Success(val shipment: Shipment) : CreateOrderState()
        data class Error(val message: String) : CreateOrderState()
    }

    private val _createOrderState = MutableStateFlow<CreateOrderState>(CreateOrderState.Idle)
    val createOrderState: StateFlow<CreateOrderState> = _createOrderState.asStateFlow()

    fun resetCreateOrderState() {
        _createOrderState.value = CreateOrderState.Idle
    }

    fun provisionOrder(shipmentId: String, deviceId: String, shipperId: String? = null) {
        viewModelScope.launch {
            _createOrderState.value = CreateOrderState.Loading
            try {
                val shipment = if (deviceId.isNotBlank()) {
                    shipmentRepository.assignDevice(shipmentId, deviceId)
                } else {
                    shipmentRepository.getShipment(shipmentId)
                }

                val finalShipment = if (shipperId != null && shipment != null) {
                    shipmentRepository.assignShipper(shipmentId, shipperId)
                } else {
                    shipment
                }

                if (finalShipment != null) {
                    _createOrderState.value = CreateOrderState.Success(finalShipment)
                    loadShipments() // Refresh shipments list
                } else {
                    _createOrderState.value = CreateOrderState.Error("Failed to provision order")
                }
            } catch (e: Exception) {
                _createOrderState.value = CreateOrderState.Error(e.message ?: "Unknown error")
            }
        }
    }
}