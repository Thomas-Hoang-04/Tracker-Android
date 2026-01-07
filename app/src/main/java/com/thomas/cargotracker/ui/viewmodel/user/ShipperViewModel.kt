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
class ShipperViewModel @Inject constructor(
    private val shipmentRepository: ShipmentRepository
) : ViewModel() {

    // Assigned Orders (Active)
    private val _assignedOrders = MutableStateFlow<List<Shipment>>(emptyList())
    val assignedOrders: StateFlow<List<Shipment>> = _assignedOrders.asStateFlow()

    // Available Orders (For "Find Order")
    private val _availableOrders = MutableStateFlow<List<Shipment>>(emptyList())
    val availableOrders: StateFlow<List<Shipment>> = _availableOrders.asStateFlow()

    // History (Completed)
    private val _historyOrders = MutableStateFlow<List<Shipment>>(emptyList())
    val historyOrders: StateFlow<List<Shipment>> = _historyOrders.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            shipmentRepository.fetchShipments()
            shipmentRepository.shipments.collect { allShipments ->
                 // Filter logic based on shipment data (e.g., status, shipperId)
                 // For now, simple split
                 _assignedOrders.value = allShipments.filter { it.status.name == "IN_TRANSIT" || it.status.name == "ASSIGNED" }
                 _availableOrders.value = allShipments.filter { it.status.name == "PENDING" }
                 _historyOrders.value = allShipments.filter { it.status.name == "DELIVERED" || it.status.name == "CANCELLED" }
            }
        }
    }
    
    fun assignOrder(shipment: Shipment) {
        // Call API to assign
        // Note: In strict flow, Provider assigns, but if Shipper "gets" order, it might be a self-assign or request.
        // Leaving as placeholder or implementing if needed, but focus is startTransit.
    }

    fun startTransit(shipmentId: String) {
        viewModelScope.launch {
            shipmentRepository.startTransit(shipmentId)
            loadData() // Refresh
        }
    }
    
    suspend fun getOrder(id: String): Shipment? {
         return shipmentRepository.getShipment(id)
    }
}
