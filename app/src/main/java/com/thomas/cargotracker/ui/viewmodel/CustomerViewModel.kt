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
class CustomerViewModel @Inject constructor() : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderSummary>>(emptyList())
    val orders: StateFlow<List<OrderSummary>> = _orders.asStateFlow()

    // Mock Providers/Users for "Search People"
    data class UserResult(val name: String, val id: String, val role: String)
    private val _users = MutableStateFlow<List<UserResult>>(emptyList())
    val users: StateFlow<List<UserResult>> = _users.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        viewModelScope.launch {
            // Mock Orders
            _orders.value = listOf(
                OrderSummary(
                    id = "ORD-2024-001",
                    customerName = "Self",
                    productType = "Electronics",
                    status = "In Transit",
                    createdDate = System.currentTimeMillis() - 86400000,
                    temperature = "24°C",
                    humidity = "45%"
                ),
                OrderSummary(
                    id = "ORD-2024-002",
                    customerName = "Self",
                    productType = "Furniture",
                    status = "Delivered",
                    createdDate = System.currentTimeMillis() - 172800000,
                    temperature = "N/A",
                    humidity = "N/A"
                ),
                 OrderSummary(
                    id = "ORD-2024-003",
                    customerName = "Self",
                    productType = "Perishables",
                    status = "Pending",
                    createdDate = System.currentTimeMillis(),
                    temperature = "4°C",
                    humidity = "60%"
                )
            )

            // Mock Users
            _users.value = listOf(
                UserResult("Fast Logistics Inc.", "PROV-001", "Provider"),
                UserResult("Secure Ship", "PROV-002", "Provider"),
                UserResult("Global Transport", "PROV-003", "Provider"),
                UserResult("Alice Driver", "DRV-001", "Driver")
            )
        }
    }
}
