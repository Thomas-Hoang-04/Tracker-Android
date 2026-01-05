package com.thomas.cargotracker.repository

import com.thomas.cargotracker.data.model.OrderSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockOrderRepository @Inject constructor() {
    private val _orders = MutableStateFlow<List<OrderSummary>>(
        listOf(
            OrderSummary(
                id = "ORD-2023-001",
                customerName = "Alice Smith",
                productType = "Electronics",
                status = "Delivered",
                temperature = "22°C",
                humidity = "45%"
            ),
             OrderSummary(
                id = "ORD-2023-002",
                customerName = "Bob Jones",
                productType = "Furniture",
                status = "In Transit",
                temperature = "24°C",
                humidity = "50%"
            )
        )
    )
    val orders: StateFlow<List<OrderSummary>> = _orders.asStateFlow()

    fun addOrder(order: OrderSummary) {
        _orders.update { current ->
            listOf(order) + current
        }
    }
    
    fun getOrder(id: String): OrderSummary? {
        return _orders.value.find { it.id == id }
    }
}
