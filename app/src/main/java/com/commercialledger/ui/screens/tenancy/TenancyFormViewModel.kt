package com.commercialledger.ui.screens.tenancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.commercialledger.data.model.Tenant
import com.commercialledger.data.model.Tenancy
import com.commercialledger.data.model.TenancyStatus
import com.commercialledger.data.repository.LedgerRepository
import com.commercialledger.ui.components.parseAmountToPaise
import com.commercialledger.ui.components.paiToDisplayString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TenancyFormState(
    val tenantName: String = "",
    val phone: String = "",
    val email: String = "",
    val idProofType: String = "",
    val idProofNumber: String = "",
    val tenantAddress: String = "",
    val startDate: String = LocalDate.now().toString(),
    val endDate: String = "",
    val monthlyRent: String = "",
    val securityDeposit: String = "",
    val taxAmount: String = "",
    val agreementDocPath: String = ""
)

class TenancyFormViewModel(
    private val roomId: Long,
    private val tenancyId: Long,
    private val repository: LedgerRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(TenancyFormState())
    val formState: StateFlow<TenancyFormState> = _formState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _savedSuccessfully = MutableStateFlow(false)
    val savedSuccessfully: StateFlow<Boolean> = _savedSuccessfully.asStateFlow()

    private var existingTenancy: Tenancy? = null
    private var existingTenant: Tenant? = null

    init {
        if (tenancyId != 0L) {
            viewModelScope.launch {
                val tenancy = repository.getTenancyById(tenancyId)
                if (tenancy != null) {
                    existingTenancy = tenancy
                    val tenant = repository.getTenantById(tenancy.tenantId)
                    existingTenant = tenant
                    _formState.update {
                        TenancyFormState(
                            tenantName = tenant?.name ?: "",
                            phone = tenant?.phone ?: "",
                            email = tenant?.email ?: "",
                            idProofType = tenant?.idProofType ?: "",
                            idProofNumber = tenant?.idProofNumber ?: "",
                            tenantAddress = tenant?.address ?: "",
                            startDate = tenancy.startDate,
                            endDate = tenancy.endDate ?: "",
                            monthlyRent = paiToDisplayString(tenancy.monthlyRent),
                            securityDeposit = paiToDisplayString(tenancy.securityDeposit),
                            taxAmount = paiToDisplayString(tenancy.taxAmount),
                            agreementDocPath = tenancy.agreementDocPath ?: ""
                        )
                    }
                }
            }
        }
    }

    fun update(block: TenancyFormState.() -> TenancyFormState) {
        _formState.update { it.block() }
    }

    fun clearError() {
        _error.value = null
    }

    fun save() {
        val state = _formState.value
        if (state.tenantName.isBlank()) {
            _error.value = "Tenant name is required"
            return
        }
        if (state.monthlyRent.isBlank() || state.monthlyRent.toDoubleOrNull() == null) {
            _error.value = "Enter a valid monthly rent"
            return
        }
        if (state.startDate.isBlank()) {
            _error.value = "Start date is required"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            try {
                if (existingTenant != null && existingTenancy != null) {
                    // Edit path — update tenant and tenancy separately (both already exist)
                    repository.updateTenant(
                        existingTenant!!.copy(
                            name = state.tenantName.trim(),
                            phone = state.phone.trim(),
                            email = state.email.trim(),
                            idProofType = state.idProofType.trim(),
                            idProofNumber = state.idProofNumber.trim(),
                            address = state.tenantAddress.trim()
                        )
                    )
                    repository.updateTenancy(
                        existingTenancy!!.copy(
                            startDate = state.startDate,
                            endDate = state.endDate.ifBlank { null },
                            monthlyRent = parseAmountToPaise(state.monthlyRent),
                            securityDeposit = parseAmountToPaise(state.securityDeposit),
                            taxAmount = parseAmountToPaise(state.taxAmount),
                            agreementDocPath = state.agreementDocPath.ifBlank { null }
                        )
                    )
                } else {
                    // New tenancy — atomic insert of tenant + tenancy in one transaction
                    val newTenant = Tenant(
                        name = state.tenantName.trim(),
                        phone = state.phone.trim(),
                        email = state.email.trim(),
                        idProofType = state.idProofType.trim(),
                        idProofNumber = state.idProofNumber.trim(),
                        address = state.tenantAddress.trim()
                    )
                    val newTenancy = Tenancy(
                        roomId = roomId,
                        tenantId = 0L, // will be set atomically in repository
                        startDate = state.startDate,
                        endDate = state.endDate.ifBlank { null },
                        monthlyRent = parseAmountToPaise(state.monthlyRent),
                        securityDeposit = parseAmountToPaise(state.securityDeposit),
                        taxAmount = parseAmountToPaise(state.taxAmount),
                        agreementDocPath = state.agreementDocPath.ifBlank { null },
                        status = TenancyStatus.ACTIVE
                    )
                    repository.insertTenantAndTenancy(newTenant, newTenancy)
                        .onFailure { e -> throw e }
                }
                _savedSuccessfully.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Save failed. Please try again."
            } finally {
                _isSaving.value = false
            }
        }
    }

    companion object {
        fun factory(roomId: Long, tenancyId: Long, repository: LedgerRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TenancyFormViewModel(roomId, tenancyId, repository) as T
            }
    }
}
