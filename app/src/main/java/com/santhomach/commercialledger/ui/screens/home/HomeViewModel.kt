package com.santhomach.commercialledger.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.santhomach.commercialledger.data.model.Complex
import com.santhomach.commercialledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(private val repository: LedgerRepository) : ViewModel() {

    val complexes: StateFlow<List<Complex>> = repository.getAllComplexesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val now = LocalDate.now()

    val totalThisMonth: StateFlow<Long> = repository.getTotalForMonthFlow(now.monthValue, now.year)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalThisYear: StateFlow<Long> = repository.getTotalForYearFlow(now.year)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalAllTime: StateFlow<Long> = repository.getTotalAllTimeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun addComplex(name: String, address: String, description: String) {
        viewModelScope.launch {
            repository.insertComplex(Complex(name = name, address = address, description = description))
        }
    }

    fun deleteComplex(complex: Complex) {
        viewModelScope.launch { repository.deleteComplex(complex) }
    }

    companion object {
        fun factory(repository: LedgerRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(repository) as T
        }
    }
}
