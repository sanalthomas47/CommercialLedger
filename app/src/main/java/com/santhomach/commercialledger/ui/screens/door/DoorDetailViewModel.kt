package com.santhomach.commercialledger.ui.screens.door

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.santhomach.commercialledger.data.model.Expense
import com.santhomach.commercialledger.data.model.RentPayment
import com.santhomach.commercialledger.data.model.RoomUnit
import com.santhomach.commercialledger.data.model.Tenancy
import com.santhomach.commercialledger.data.model.Tenant
import com.santhomach.commercialledger.data.repository.LedgerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DoorDetailViewModel(
    private val doorId: Long,
    private val repository: LedgerRepository
) : ViewModel() {

    private val _room = MutableStateFlow<RoomUnit?>(null)
    val roomState: StateFlow<RoomUnit?> = _room.asStateFlow()

    val activeTenancy: StateFlow<Tenancy?> = repository.getActiveTenancyForRoomFlow(doorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTenant: StateFlow<Tenant?> = activeTenancy
        .filterNotNull()
        .flatMapLatest { tenancy ->
            flow { emit(repository.getTenantById(tenancy.tenantId)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tenantAllDoors: StateFlow<List<String>> = activeTenancy
        .flatMapLatest { tenancy ->
            if (tenancy == null) flowOf(emptyList())
            else repository.getActiveDoorNumbersForTenant(tenancy.tenantId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All payments of the active tenancy, newest rent month first. The screen shows
    // the latest few; the full list is also needed for per-month totals.
    @OptIn(ExperimentalCoroutinesApi::class)
    val payments: StateFlow<List<RentPayment>> = activeTenancy
        .flatMapLatest { tenancy ->
            if (tenancy == null) flowOf(emptyList())
            else repository.getPaymentsForTenancyFlow(tenancy.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doorExpenses: StateFlow<List<Expense>> = repository.getExpensesForRoomFlow(doorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    init {
        viewModelScope.launch {
            _room.value = repository.getRoomUnitById(doorId)
        }
    }

    fun clearError() { _error.value = null }
    fun clearSuccess() { _successMessage.value = null }

    fun updateRoom(doorNumber: String, floor: String, description: String) {
        viewModelScope.launch {
            _room.value?.let { room ->
                val updated = room.copy(doorNumber = doorNumber, floor = floor, description = description)
                repository.updateRoomUnit(updated)
                _room.value = updated
            }
        }
    }

    fun deleteRoom() {
        viewModelScope.launch {
            _room.value?.let {
                repository.deleteRoomUnit(it)
                _deleted.value = true
            }
        }
    }

    fun deletePayment(payment: RentPayment) {
        viewModelScope.launch { repository.deletePayment(payment) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun recordPayment(amountPaise: Long, paymentDate: String, month: Int, year: Int, paymentMode: String, notes: String) {
        val tenancy = activeTenancy.value
        if (tenancy == null) {
            _error.value = "No active tenancy. Please add a tenant first."
            return
        }
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            repository.insertPayment(
                RentPayment(
                    tenancyId = tenancy.id,
                    paymentDate = paymentDate,
                    amountPaid = amountPaise,
                    month = month,
                    year = year,
                    paymentMode = paymentMode,
                    notes = notes
                )
            ).onSuccess {
                _successMessage.value = "Payment recorded"
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to record payment"
            }
            _isProcessing.value = false
        }
    }

    fun updatePayment(payment: RentPayment) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            repository.updatePayment(payment)
                .onSuccess { _successMessage.value = "Payment updated" }
                .onFailure { e -> _error.value = e.message ?: "Failed to update payment" }
            _isProcessing.value = false
        }
    }

    fun closeTenancy(endDate: String, notes: String, refundAmount: Long) {
        val tenancy = activeTenancy.value
        if (tenancy == null) {
            _error.value = "No active tenancy to close."
            return
        }
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            repository.closeTenancy(
                tenancyId = tenancy.id,
                endDate = endDate,
                closureNotes = notes,
                refundAmount = refundAmount
            ).onSuccess {
                _successMessage.value = "Tenancy closed successfully"
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to close tenancy"
            }
            _isProcessing.value = false
        }
    }

    companion object {
        fun factory(doorId: Long, repository: LedgerRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DoorDetailViewModel(doorId, repository) as T
            }
    }
}
