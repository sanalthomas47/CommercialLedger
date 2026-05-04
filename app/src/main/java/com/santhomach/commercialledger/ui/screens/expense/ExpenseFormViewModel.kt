package com.santhomach.commercialledger.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.santhomach.commercialledger.data.model.Expense
import com.santhomach.commercialledger.data.model.ExpenseType
import com.santhomach.commercialledger.data.repository.LedgerRepository
import com.santhomach.commercialledger.ui.components.parseAmountToPaise
import com.santhomach.commercialledger.ui.components.paiToDisplayString
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExpenseFormState(
    val amount: String = "",
    val date: String = LocalDate.now().toString(),
    val category: String = "",
    val description: String = "",
    val type: ExpenseType = ExpenseType.COMPLEX
)

class ExpenseFormViewModel(
    private val complexId: Long,
    private val roomId: Long,
    private val expenseId: Long,
    private val repository: LedgerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        ExpenseFormState(type = if (roomId != 0L) ExpenseType.DOOR else ExpenseType.COMPLEX)
    )
    val state: StateFlow<ExpenseFormState> = _state.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _savedSuccessfully = MutableStateFlow(false)
    val savedSuccessfully: StateFlow<Boolean> = _savedSuccessfully.asStateFlow()

    private var existing: Expense? = null

    init {
        if (expenseId != 0L) {
            viewModelScope.launch {
                val expense = repository.getExpenseById(expenseId)
                if (expense != null) {
                    existing = expense
                    _state.value = ExpenseFormState(
                        amount = paiToDisplayString(expense.amount),
                        date = expense.date,
                        category = expense.category,
                        description = expense.description,
                        type = expense.type
                    )
                }
            }
        }
    }

    fun update(block: ExpenseFormState.() -> ExpenseFormState) {
        _state.value = _state.value.block()
    }

    fun save() {
        val s = _state.value
        if (s.amount.isBlank() || s.amount.toDoubleOrNull() == null) {
            _error.value = "Valid amount is required"
            return
        }
        if (s.date.isBlank()) {
            _error.value = "Date is required"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                val amountPaise = parseAmountToPaise(s.amount)
                val parsedDate = s.date
                val localDate = java.time.LocalDate.parse(parsedDate)
                val month = localDate.monthValue
                val year = localDate.year

                if (existing != null) {
                    repository.updateExpense(
                        existing!!.copy(
                            amount = amountPaise,
                            date = parsedDate,
                            month = month,
                            year = year,
                            category = s.category.trim(),
                            description = s.description.trim(),
                            type = s.type
                        )
                    )
                } else {
                    repository.insertExpense(
                        Expense(
                            complexId = complexId,
                            roomId = if (roomId != 0L) roomId else null,
                            amount = amountPaise,
                            date = parsedDate,
                            month = month,
                            year = year,
                            category = s.category.trim(),
                            description = s.description.trim(),
                            type = if (roomId != 0L) ExpenseType.DOOR else s.type
                        )
                    )
                }
                _savedSuccessfully.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Save failed"
            } finally {
                _isSaving.value = false
            }
        }
    }

    companion object {
        fun factory(complexId: Long, roomId: Long, expenseId: Long, repository: LedgerRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ExpenseFormViewModel(complexId, roomId, expenseId, repository) as T
            }
    }
}
