package com.thomas.cargotracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.data.model.OrderSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShipperViewModel @Inject constructor() : ViewModel() {

    // Assigned Orders (Active)
    private val _assignedOrders = MutableStateFlow<List<OrderSummary>>(emptyList())
    val assignedOrders: StateFlow<List<OrderSummary>> = _assignedOrders.asStateFlow()

    // Available Orders (For "Find Order")
    private val _availableOrders = MutableStateFlow<List<OrderSummary>>(emptyList())
    val availableOrders: StateFlow<List<OrderSummary>> = _availableOrders.asStateFlow()

    // History (Completed)
    private val _historyOrders = MutableStateFlow<List<OrderSummary>>(emptyList())
    val historyOrders: StateFlow<List<OrderSummary>> = _historyOrders.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        viewModelScope.launch {
            // Mock Assigned
            _assignedOrders.value = listOf(
                OrderSummary(
                    id = "SHP-001",
                    customerName = "ElectroWorld",
                    productType = "Components",
                    status = "In Transit",
                    createdDate = System.currentTimeMillis() - 3600000,
                    temperature = "22°C",
                    humidity = "40%"
                )
            )

            // Mock Available
            _availableOrders.value = listOf(
                OrderSummary(
                    id = "AV-101",
                    customerName = "Fresh Foods",
                    productType = "Perishables",
                    status = "Pending",
                    createdDate = System.currentTimeMillis() - 7200000,
                    temperature = "4°C",
                    humidity = "60%"
                ),
                OrderSummary(
                    id = "AV-102",
                    customerName = "MegaBuild",
                    productType = "Construction",
                    status = "Pending",
                    createdDate = System.currentTimeMillis() - 10800000,
                    temperature = "N/A",
                    humidity = "N/A"
                )
            )
            
            // Mock History
             _historyOrders.value = listOf(
                OrderSummary(
                    id = "HIS-999",
                    customerName = "Old Client",
                    productType = "Books",
                    status = "Delivered",
                    createdDate = System.currentTimeMillis() - 99999999,
                    temperature = "N/A",
                    humidity = "N/A"
                )
            )
        }
    }

    fun assignOrder(order: OrderSummary) {
        val currentAvailable = _availableOrders.value.toMutableList()
        val currentAssigned = _assignedOrders.value.toMutableList()

        if (currentAvailable.remove(order)) {
            val updatedOrder = order.copy(status = "In Transit") // Simulate starting
            currentAssigned.add(0, updatedOrder)
            
            _availableOrders.value = currentAvailable
            _assignedOrders.value = currentAssigned
        }
    }
}
