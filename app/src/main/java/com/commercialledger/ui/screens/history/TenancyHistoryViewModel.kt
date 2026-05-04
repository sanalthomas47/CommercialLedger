package com.commercialledger.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.commercialledger.data.model.RoomUnit
import com.commercialledger.data.model.Tenancy
import com.commercialledger.data.model.Tenant
import com.commercialledger.data.repository.LedgerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TenancyHistoryViewModel(
    private val roomId: Long,
    private val repository: LedgerRepository
) : ViewModel() {

    val room: StateFlow<RoomUnit?> = MutableStateFlow<RoomUnit?>(null).also { flow ->
        viewModelScope.launch { (flow as MutableStateFlow).value = repository.getRoomUnitById(roomId) }
    }.asStateFlow()

    val tenancies: StateFlow<List<Tenancy>> = repository.getTenancyHistoryForRoomFlow(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tenantMap: StateFlow<Map<Long, Tenant>> = tenancies.flatMapLatest { list ->
        if (list.isEmpty()) flowOf(emptyMap())
        else flow {
            val map = list.mapNotNull { t ->
                repository.getTenantById(t.tenantId)?.let { tenant -> t.tenantId to tenant }
            }.toMap()
            emit(map)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    companion object {
        fun factory(roomId: Long, repository: LedgerRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TenancyHistoryViewModel(roomId, repository) as T
            }
    }
}
