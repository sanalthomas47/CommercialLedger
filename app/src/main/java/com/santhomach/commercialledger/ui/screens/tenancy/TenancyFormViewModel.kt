package com.santhomach.commercialledger.ui.screens.tenancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.santhomach.commercialledger.data.model.Tenant
import com.santhomach.commercialledger.data.model.Tenancy
import com.santhomach.commercialledger.data.model.TenancyStatus
import com.santhomach.commercialledger.data.repository.LedgerRepository
import com.santhomach.commercialledger.ui.components.parseAmountToPaise
import com.santhomach.commercialledger.ui.components.paiToDisplayString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    private val _useExistingTenant = MutableStateFlow(false)
    val useExistingTenant: StateFlow<Boolean> = _useExistingTenant.asStateFlow()

    private val _selectedExistingTenant = MutableStateFlow<Tenant?>(null)
    val selectedExistingTenant: StateFlow<Tenant?> = _selectedExistingTenant.asStateFlow()

    val allTenants: StateFlow<List<Tenant>> = repository.getAllTenantsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun setUseExistingTenant(value: Boolean) {
        _useExistingTenant.value = value
        if (!value) _selectedExistingTenant.value = null
    }

    fun selectExistingTenant(tenant: Tenant) {
        _selectedExistingTenant.value = tenant
    }

    fun clearError() {
        _error.value = null
    }

    fun save() {
        val state = _formState.value
        val isExistingMode = _useExistingTenant.value

        if (existingTenant == null && existingTenancy == null) {
            if (isExistingMode) {
                if (_selectedExistingTenant.value == null) {
                    _error.value = "Please select a tenant"
                    return
                }
            } else {
                if (state.tenantName.isBlank()) {
                    _error.value = "Tenant name is required"
                    return
                }
            }
        } else {
            if (state.tenantName.isBlank()) {
                _error.value = "Tenant name is required"
                return
            }
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
                when {
                    existingTenant != null && existingTenancy != null -> {
                        // Edit path — update tenant and tenancy separately
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
                    }
                    isExistingMode -> {
                        // Existing tenant, new door assignment
                        val tenant = _selectedExistingTenant.value!!
                        repository.insertTenancyForExistingTenant(
                            Tenancy(
                                roomId = roomId,
                                tenantId = tenant.id,
                                startDate = state.startDate,
                                endDate = state.endDate.ifBlank { null },
                                monthlyRent = parseAmountToPaise(state.monthlyRent),
                                securityDeposit = parseAmountToPaise(state.securityDeposit),
                                taxAmount = parseAmountToPaise(state.taxAmount),
                                agreementDocPath = state.agreementDocPath.ifBlank { null },
                                status = TenancyStatus.ACTIVE
                            )
                        ).onFailure { e -> throw e }
                    }
                    else -> {
                        // New tenant + new tenancy
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
                            tenantId = 0L,
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
