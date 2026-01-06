package com.thomas.cargotracker.ui.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.domain.model.Shipment
import com.thomas.cargotracker.repository.ShipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val shipmentRepository: ShipmentRepository
) : ViewModel() {

    val orders: StateFlow<List<Shipment>> = shipmentRepository.shipments

    // Mock Providers/Users for "Search People"
    data class UserResult(val name: String, val id: String, val role: String)
    private val _users = MutableStateFlow<List<UserResult>>(emptyList())
    val users: StateFlow<List<UserResult>> = _users.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            shipmentRepository.fetchShipments()

            // Mock Users (Keep as is for now)
            _users.value = listOf(
                UserResult("Pham Viet Hoa", "97b9e86c-e958-4fe9-87ce-45f62994954a", "Customer"),
                UserResult("Provider Company", "e811533d-1f5a-4eee-9456-b33d682969d8", "Provider"),
                UserResult("Shipper", "71ad6c6a-c19d-493a-9f4d-21b2edcab276", "Shipper"),
            )
        }
    }
    
    sealed class CreateOrderState {
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
        estimatedDeliveryAt: String? = null
    ) {
        viewModelScope.launch {
            _createOrderState.value = CreateOrderState.Loading
            try {
                // Customer uses the default createShipment which relies on backend principal for customerId
                val shipment = shipmentRepository.createShipment(
                    providerId = providerId,
                    goodsDescription = goodsDescription,
                    pickupAddress = pickupAddress,
                    deliveryAddress = deliveryAddress,
                    estimatedDeliveryAt = estimatedDeliveryAt
                )
                
                if (shipment != null) {
                    _createOrderState.value = CreateOrderState.Success
                    loadData() // Refresh list
                } else {
                    _createOrderState.value = CreateOrderState.Error("Failed to create order")
                }
            } catch (e: Exception) {
                _createOrderState.value = CreateOrderState.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getOrder(id: String): Shipment? {
        return shipmentRepository.getShipment(id)
    }
}
