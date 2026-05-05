package com.santhomach.commercialledger.ui.screens.complex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.santhomach.commercialledger.data.model.Complex
import com.santhomach.commercialledger.data.model.Expense
import com.santhomach.commercialledger.data.model.RoomUnit
import com.santhomach.commercialledger.data.model.Tenancy
import kotlinx.coroutines.flow.MutableStateFlow
import com.santhomach.commercialledger.data.repository.LedgerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class ComplexDetailViewModel(
    private val complexId: Long,
    private val repository: LedgerRepository
) : ViewModel() {

    val complex: StateFlow<Complex?> = repository.getAllComplexesFlow()
        .map { list -> list.find { it.id == complexId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rooms: StateFlow<List<RoomUnit>> = repository.getRoomsForComplexFlow(complexId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTenancyMap: StateFlow<Map<Long, Tenancy?>> = rooms.flatMapLatest { roomList ->
        if (roomList.isEmpty()) flowOf(emptyMap())
        else combine(roomList.map { room ->
            repository.getActiveTenancyForRoomFlow(room.id).map { tenancy -> room.id to tenancy }
        }) { pairs -> pairs.toMap() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val expectedMonthlyRent: StateFlow<Long> = repository.getExpectedMonthlyRentForComplex(complexId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val now = LocalDate.now()

    val collectedThisMonth: StateFlow<Long> = repository
        .getCollectedRentForComplex(complexId, now.monthValue, now.year)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val complexExpenses: StateFlow<List<Expense>> = repository.getExpensesForComplexFlow(complexId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun addRoom(doorNumber: String, floor: String, description: String) {
        viewModelScope.launch {
            repository.insertRoomUnit(
                RoomUnit(
                    complexId = complexId,
                    doorNumber = doorNumber,
                    floor = floor,
                    description = description
                )
            )
        }
    }

    fun deleteRoom(room: RoomUnit) {
        viewModelScope.launch { repository.deleteRoomUnit(room) }
    }

    fun updateRoom(room: RoomUnit) {
        viewModelScope.launch { repository.updateRoomUnit(room) }
    }

    fun updateComplex(name: String, address: String, description: String) {
        viewModelScope.launch {
            complex.value?.let {
                repository.updateComplex(it.copy(name = name, address = address, description = description))
            }
        }
    }

    fun deleteComplex() {
        viewModelScope.launch {
            complex.value?.let {
                repository.deleteComplex(it)
                _deleted.value = true
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    companion object {
        fun factory(complexId: Long, repository: LedgerRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ComplexDetailViewModel(complexId, repository) as T
            }
    }
}
