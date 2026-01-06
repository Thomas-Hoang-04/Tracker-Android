package com.thomas.cargotracker.ui.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.repository.ShipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.thomas.cargotracker.data.model.ShipmentStatus
import javax.inject.Inject

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val shipmentRepository: ShipmentRepository
) : ViewModel() {

    val orders: StateFlow<List<Shipment>> = shipmentRepository.shipments

    // Filtering State
    data class FilterState(
        val status: ShipmentStatus? = null,
        val search: String = ""
    )
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            shipmentRepository.filterShipments(
                status = _filterState.value.status,
                search = _filterState.value.search.ifBlank { null }
            )
        }
    }

    fun updateStatusFilter(status: ShipmentStatus?) {
        _filterState.update { it.copy(status = status) }
        loadOrders()
    }

    fun updateSearchFilter(search: String) {
        _filterState.update { it.copy(search = search) }
        loadOrders()
    }
    
    fun applySearch() {
        loadOrders()
    }
    
    fun clearFilters() {
        _filterState.value = FilterState()
        loadOrders()
    }

    // Create Order State
    sealed class CreateOrderState { // keeping name to avoid heavy refactor in UI for now, effectively "ProvisionState"
        object Idle : CreateOrderState()
        object Loading : CreateOrderState()
        object Success : CreateOrderState()
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
        deviceId: String? = null,
        shipperId: String? = null
    ) {
        viewModelScope.launch {
            _createOrderState.value = CreateOrderState.Loading
            try {
                val shipment = shipmentRepository.createShipment(
                    providerId = providerId,
                    goodsDescription = goodsDescription,
                    pickupAddress = pickupAddress,
                    deliveryAddress = deliveryAddress,
                    estimatedDeliveryAt = estimatedDeliveryAt
                )
                
                if (shipment != null) {
                    if (deviceId != null) {
                        shipmentRepository.assignDevice(shipment.id, deviceId)
                    }
                    if (shipperId != null) {
                        shipmentRepository.assignShipper(shipment.id, shipperId)
                    }
                    _createOrderState.value = CreateOrderState.Success
                    loadOrders() 
                } else {
                    _createOrderState.value = CreateOrderState.Error("Failed to create order")
                }
            } catch (e: Exception) {
                _createOrderState.value = CreateOrderState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun provisionOrder(
        shipmentId: String,
        deviceId: String,
        shipperId: String? = null
    ) {
        viewModelScope.launch {
            _createOrderState.value = CreateOrderState.Loading
            try {
                shipmentRepository.assignDevice(shipmentId, deviceId)
                
                if (shipperId != null) {
                    shipmentRepository.assignShipper(shipmentId, shipperId)
                }

                _createOrderState.value = CreateOrderState.Success
                loadOrders()
            } catch (e: Exception) {
                _createOrderState.value = CreateOrderState.Error(e.message ?: "Failed to provision order")
            }
        }
    }
    
    suspend fun getOrder(id: String): Shipment? {
        return shipmentRepository.getShipment(id)
    }

    // Actions
    fun assignShipper(shipmentId: String, shipperId: String) {
        viewModelScope.launch {
            shipmentRepository.assignShipper(shipmentId, shipperId)
        }
    }

    fun assignDevice(shipmentId: String, deviceId: String) {
        viewModelScope.launch {
            shipmentRepository.assignDevice(shipmentId, deviceId)
        }
    }

    fun startTransit(shipmentId: String) {
        viewModelScope.launch {
            shipmentRepository.startTransit(shipmentId)
        }
    }

    fun completeShipment(shipmentId: String, deliveredAt: String? = null) {
        viewModelScope.launch {
            shipmentRepository.completeShipment(shipmentId, deliveredAt)
        }
    }

    fun cancelShipment(shipmentId: String, reason: String) {
        viewModelScope.launch {
            shipmentRepository.cancelShipment(shipmentId, reason)
        }
    }
}
