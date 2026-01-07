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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.thomas.cargotracker.repository.UserRepository
import com.thomas.cargotracker.dto.UserRole
import com.thomas.cargotracker.network.Result

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val shipmentRepository: ShipmentRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Filtering State
    data class FilterState(
        val status: ShipmentStatus? = null,
        val search: String = ""
    )
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Shipments for Provider (existing) - Client-side filtered
    val shipments: StateFlow<List<Shipment>> = combine(
        shipmentRepository.shipments,
        _filterState
    ) { shipments, filter ->
        if (filter.search.isBlank()) {
            shipments
        } else {
            val query = filter.search.lowercase()
            shipments.filter {
                it.description.lowercase().contains(query) ||
                it.origin.lowercase().contains(query) ||
                it.destination.lowercase().contains(query) ||
                it.trackingId.lowercase().contains(query)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Shippers List
    data class UserResult(val name: String, val id: String)
    private val _shippers = MutableStateFlow<List<UserResult>>(emptyList())
    val shippers: StateFlow<List<UserResult>> = _shippers.asStateFlow()

    // Pending Orders for Provider approval
    val pendingOrders: StateFlow<List<OrderResponse>> = orderRepository.pendingOrders

    init {
        loadShipments()
        fetchShippers()
    }

    private fun fetchShippers() {
        viewModelScope.launch {
            try {
                // Assuming UserRole.SHIPPER is accessible here, might need import or mapping
                // Using UserRepository directly
                when (val result = userRepository.getUsersByRole(UserRole.SHIPPER)) {
                   is Result.Success -> {
                       _shippers.value = result.data
                           .filter { it.isActive }
                           .map { UserResult(it.fullName, it.id) }
                   }
                   else -> _shippers.value = emptyList()
                }
            } catch (e: Exception) {
               _shippers.value = emptyList()
            }
        }
    }

    fun loadShipments() {
        viewModelScope.launch {
            // Pass null for search to avoid backend lower(bytea) error
            shipmentRepository.filterShipments(
                status = _filterState.value.status,
                search = null 
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
                // 1. Assign Shipper First (Transitions status from PENDING -> ASSIGNED)
                val shippedShipment = if (shipperId != null) {
                    shipmentRepository.assignShipper(shipmentId, shipperId)
                } else {
                    shipmentRepository.getShipment(shipmentId)
                }

                // 2. Assign Device Second (Requires status ASSIGNED)
                val finalShipment = if (deviceId.isNotBlank() && shippedShipment != null) {
                    shipmentRepository.assignDevice(shipmentId, deviceId)
                } else {
                    shippedShipment
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