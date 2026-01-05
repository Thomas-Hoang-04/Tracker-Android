package com.thomas.cargotracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.data.model.OrderSummary
import com.thomas.cargotracker.repository.MockOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val orderRepository: MockOrderRepository
) : ViewModel() {

    val orders: StateFlow<List<OrderSummary>> = orderRepository.orders

    fun createOrder(order: OrderSummary) {
        viewModelScope.launch {
            orderRepository.addOrder(order)
        }
    }
    
    fun getOrder(id: String): OrderSummary? {
        return orderRepository.getOrder(id)
    }
}
