package com.santhomach.commercialledger.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.santhomach.commercialledger.data.model.Complex
import com.santhomach.commercialledger.data.repository.LedgerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate

data class ComplexSummary(
    val complex: Complex,
    val expectedMonthlyRent: Long,
    val collectedThisMonth: Long,
    val collectedThisYear: Long,
    val allTimeCollected: Long,
    val totalExpensesThisYear: Long,
    val netIncomeThisYear: Long
)

data class GlobalSummary(
    val totalCollectedThisMonth: Long,
    val totalCollectedThisYear: Long,
    val allTimeCollected: Long,
    val totalExpensesThisYear: Long,
    val netIncomeThisYear: Long
)

class SummaryViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(LocalDate.now().monthValue)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    val complexes: StateFlow<List<Complex>> = repository.getAllComplexesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val perComplexSummary: StateFlow<List<ComplexSummary>> = combine(
        complexes, _selectedMonth, _selectedYear
    ) { list, month, year -> Triple(list, month, year) }
        .flatMapLatest { (complexList, month, year) ->
            if (complexList.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(
                complexList.map { complex ->
                    combine(
                        repository.getExpectedMonthlyRentForComplex(complex.id),
                        repository.getCollectedRentForComplex(complex.id, month, year),
                        repository.getTotalForYearByComplexFlow(complex.id, year),
                        repository.getTotalAllTimeByComplexFlow(complex.id),
                        repository.getExpenseTotalForComplexYear(complex.id, year)
                    ) { expected, collectedMonth, collectedYear, allTime, expenses ->
                        ComplexSummary(
                            complex = complex,
                            expectedMonthlyRent = expected,
                            collectedThisMonth = collectedMonth,
                            collectedThisYear = collectedYear,
                            allTimeCollected = allTime,
                            totalExpensesThisYear = expenses,
                            netIncomeThisYear = collectedYear - expenses
                        )
                    }
                }
            ) { summaries -> summaries.toList() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalSummary: StateFlow<GlobalSummary> = combine(
        _selectedMonth,
        _selectedYear,
        perComplexSummary
    ) { month, year, summaries ->
        GlobalSummary(
            totalCollectedThisMonth = summaries.sumOf { it.collectedThisMonth },
            totalCollectedThisYear = summaries.sumOf { it.collectedThisYear },
            allTimeCollected = summaries.sumOf { it.allTimeCollected },
            totalExpensesThisYear = summaries.sumOf { it.totalExpensesThisYear },
            netIncomeThisYear = summaries.sumOf { it.netIncomeThisYear }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlobalSummary(0, 0, 0, 0, 0))

    fun selectMonth(month: Int) { _selectedMonth.value = month }
    fun selectYear(year: Int) { _selectedYear.value = year }

    companion object {
        fun factory(repository: LedgerRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SummaryViewModel(repository) as T
        }
    }
}
