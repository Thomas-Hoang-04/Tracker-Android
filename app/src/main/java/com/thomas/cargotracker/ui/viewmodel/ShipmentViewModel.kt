package com.thomas.cargotracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thomas.cargotracker.dto.ShipmentResponse
import com.thomas.cargotracker.dto.ShipmentStatus
import com.thomas.cargotracker.network.Result
import com.thomas.cargotracker.repository.ShipmentRepository
import com.thomas.cargotracker.ui.state.CreateShipmentState
import com.thomas.cargotracker.ui.state.ShipmentActionState
import com.thomas.cargotracker.ui.state.ShipmentDetailState
import com.thomas.cargotracker.ui.state.ShipmentFilterState
import com.thomas.cargotracker.ui.state.ShipmentListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShipmentViewModel @Inject constructor(
    private val shipmentRepository: ShipmentRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(ShipmentListState())
    val listState: StateFlow<ShipmentListState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(ShipmentDetailState())
    val detailState: StateFlow<ShipmentDetailState> = _detailState.asStateFlow()

    private val _createState = MutableStateFlow(CreateShipmentState())
    val createState: StateFlow<CreateShipmentState> = _createState.asStateFlow()

    private val _filterState = MutableStateFlow(ShipmentFilterState())
    val filterState: StateFlow<ShipmentFilterState> = _filterState.asStateFlow()

    private val _actionState = MutableStateFlow(ShipmentActionState())
    val actionState: StateFlow<ShipmentActionState> = _actionState.asStateFlow()

    private val pageSize = 20

    fun loadShipments(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _listState.update { it.copy(isRefreshing = true, currentPage = 1) }
            } else {
                _listState.update { it.copy(isLoading = true) }
            }

            val filter = _filterState.value
            when (val result = shipmentRepository.filterShipments(
                status = filter.status,
                search = filter.search.ifBlank { null },
                page = if (refresh) 1 else _listState.value.currentPage,
                pageSize = pageSize,
                sortBy = filter.sortBy,
                sortOrder = filter.sortOrder
            )) {
                is Result.Success -> {
                    val data = result.data
                    _listState.update {
                        it.copy(
                            shipments = if (refresh) data.shipments else it.shipments + data.shipments,
                            isLoading = false,
                            isRefreshing = false,
                            currentPage = data.page,
                            totalPages = data.totalPages,
                            hasMore = data.page < data.totalPages,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _listState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun loadMoreShipments() {
        val state = _listState.value
        if (state.isLoading || !state.hasMore) return

        _listState.update { it.copy(currentPage = it.currentPage + 1) }
        loadShipments()
    }

    fun refreshShipments() {
        loadShipments(refresh = true)
    }

    fun loadShipmentById(id: String) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            when (val result = shipmentRepository.getShipmentById(id)) {
                is Result.Success -> {
                    _detailState.update {
                        it.copy(isLoading = false, shipment = result.data, error = null)
                    }
                }
                is Result.Error -> {
                    _detailState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    // Filter
    fun updateStatusFilter(status: ShipmentStatus?) {
        _filterState.update { it.copy(status = status) }
        loadShipments(refresh = true)
    }

    fun updateSearchFilter(search: String) {
        _filterState.update { it.copy(search = search) }
    }

    fun applySearch() {
        loadShipments(refresh = true)
    }

    fun clearFilters() {
        _filterState.value = ShipmentFilterState()
        loadShipments(refresh = true)
    }

    // Create Shipment
    fun updateProviderId(providerId: String) {
        _createState.update { it.copy(providerId = providerId, error = null) }
    }

    fun updateGoodsDescription(description: String) {
        _createState.update { it.copy(goodsDescription = description, error = null) }
    }

    fun updatePickupAddress(address: String) {
        _createState.update { it.copy(pickupAddress = address, error = null) }
    }

    fun updateDeliveryAddress(address: String) {
        _createState.update { it.copy(deliveryAddress = address, error = null) }
    }

    fun updateEstimatedDeliveryAt(dateTime: String) {
        _createState.update { it.copy(estimatedDeliveryAt = dateTime, error = null) }
    }

    fun createShipment() {
        val state = _createState.value
        if (state.providerId.isBlank() || state.goodsDescription.isBlank() ||
            state.pickupAddress.isBlank() || state.deliveryAddress.isBlank()) {
            _createState.update { it.copy(error = "Please fill in all required fields") }
            return
        }

        viewModelScope.launch {
            _createState.update { it.copy(isLoading = true, error = null) }
            when (val result = shipmentRepository.createShipment(
                providerId = state.providerId,
                goodsDescription = state.goodsDescription,
                pickupAddress = state.pickupAddress,
                deliveryAddress = state.deliveryAddress,
                estimatedDeliveryAt = state.estimatedDeliveryAt.ifBlank { null }
            )) {
                is Result.Success -> {
                    _createState.update {
                        it.copy(isLoading = false, isSuccess = true, createdShipment = result.data)
                    }
                    refreshShipments()
                }
                is Result.Error -> {
                    _createState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetCreateState() {
        _createState.value = CreateShipmentState()
    }

    // Actions
    fun assignShipper(shipmentId: String, shipperId: String) {
        performAction {
            shipmentRepository.assignShipper(shipmentId, shipperId)
        }
    }

    fun assignDevice(shipmentId: String, deviceId: String) {
        performAction {
            shipmentRepository.assignDevice(shipmentId, deviceId)
        }
    }

    fun startTransit(shipmentId: String) {
        performAction {
            shipmentRepository.startTransit(shipmentId)
        }
    }

    fun completeShipment(shipmentId: String, deliveredAt: String? = null) {
        performAction {
            shipmentRepository.completeShipment(shipmentId, deliveredAt)
        }
    }

    fun cancelShipment(shipmentId: String, reason: String) {
        performAction {
            shipmentRepository.cancelShipment(shipmentId, reason)
        }
    }

    private fun performAction(action: suspend () -> Result<ShipmentResponse>) {
        viewModelScope.launch {
            _actionState.update { it.copy(isLoading = true, error = null) }
            when (val result = action()) {
                is Result.Success -> {
                    _actionState.update { it.copy(isLoading = false, isSuccess = true) }
                    _detailState.update { it.copy(shipment = result.data) }
                    refreshShipments()
                }
                is Result.Error -> {
                    _actionState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ShipmentActionState()
    }

    fun clearListError() {
        _listState.update { it.copy(error = null) }
    }

    fun clearDetailError() {
        _detailState.update { it.copy(error = null) }
    }
}
