package com.santhomach.commercialledger.ui.screens.tenancy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.santhomach.commercialledger.CommercialLedgerApplication
import com.santhomach.commercialledger.ui.components.AmountField
import com.santhomach.commercialledger.ui.components.DatePickerField
import com.santhomach.commercialledger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenancyFormScreen(roomId: Long, tenancyId: Long, navController: NavHostController) {
    val app = LocalContext.current.applicationContext as CommercialLedgerApplication
    val vm: TenancyFormViewModel = viewModel(
        factory = TenancyFormViewModel.factory(roomId, tenancyId, app.container.repository)
    )

    val state by vm.formState.collectAsState()
    val isSaving by vm.isSaving.collectAsState()
    val error by vm.error.collectAsState()
    val savedSuccessfully by vm.savedSuccessfully.collectAsState()
    val useExistingTenant by vm.useExistingTenant.collectAsState()
    val selectedExistingTenant by vm.selectedExistingTenant.collectAsState()
    val allTenants by vm.allTenants.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    LaunchedEffect(savedSuccessfully) {
        if (savedSuccessfully) navController.popBackStack()
    }

    val isEdit = tenancyId != 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Tenancy" else "New Tenant") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.save() }, enabled = !isSaving && !isLoading) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Green800.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Green800, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                if (isEdit) "Edit Tenant & Agreement" else "Add New Tenant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                if (isEdit) "Update tenant details and agreement" else "Fill in details to add a tenant",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Mode toggle — only shown for new tenancy
            if (!isEdit) {
                item {
                    Spacer(Modifier.height(16.dp))
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        SegmentedButton(
                            selected = !useExistingTenant,
                            onClick = { vm.setUseExistingTenant(false) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = { SegmentedButtonDefaults.Icon(active = !useExistingTenant) }
                        ) { Text("New Tenant") }
                        SegmentedButton(
                            selected = useExistingTenant,
                            onClick = { vm.setUseExistingTenant(true) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = { SegmentedButtonDefaults.Icon(active = useExistingTenant) }
                        ) { Text("Existing Tenant") }
                    }
                }
            }

            if (useExistingTenant && !isEdit) {
                // Existing tenant picker
                item {
                    Spacer(Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        FormSectionHeader(Icons.Default.People, "Select Tenant")
                        Spacer(Modifier.height(10.dp))
                        var dropdownExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedExistingTenant?.let {
                                    if (it.phone.isNotBlank()) "${it.name} · ${it.phone}" else it.name
                                } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tenant *") },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = NeutralGray) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                                placeholder = { Text("Select an existing tenant") },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                if (allTenants.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No tenants found", color = NeutralGray) },
                                        onClick = { dropdownExpanded = false }
                                    )
                                } else {
                                    allTenants.forEach { tenant ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        tenant.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    if (tenant.phone.isNotBlank()) {
                                                        Text(
                                                            tenant.phone,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = NeutralGray
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                vm.selectExistingTenant(tenant)
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // New tenant personal details
                item {
                    Spacer(Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        FormSectionHeader(Icons.Default.Person, "Tenant Details")
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = state.tenantName,
                            onValueChange = { vm.update { copy(tenantName = it) } },
                            label = { Text("Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Badge, null, tint = NeutralGray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { vm.update { copy(phone = it) } },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = NeutralGray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { vm.update { copy(email = it) } },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = NeutralGray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val proofTypes = listOf("Aadhaar", "PAN", "Passport", "Voter ID", "Driving License", "Other")
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = state.idProofType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("ID Proof Type") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    proofTypes.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t) },
                                            onClick = { vm.update { copy(idProofType = t) }; expanded = false }
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = state.idProofNumber,
                                onValueChange = { vm.update { copy(idProofNumber = it) } },
                                label = { Text("ID Number") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.tenantAddress,
                            onValueChange = { vm.update { copy(tenantAddress = it) } },
                            label = { Text("Tenant Address") },
                            leadingIcon = { Icon(Icons.Default.Home, null, tint = NeutralGray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    FormSectionHeader(Icons.Default.Description, "Agreement Details")
                    Spacer(Modifier.height(10.dp))
                    DatePickerField(
                        value = state.startDate,
                        onValueChange = { vm.update { copy(startDate = it) } },
                        label = "Agreement Start Date",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    DatePickerField(
                        value = state.endDate,
                        onValueChange = { vm.update { copy(endDate = it) } },
                        label = "Agreement End Date (optional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    FormSectionHeader(Icons.Default.CurrencyRupee, "Financial Details")
                    Spacer(Modifier.height(10.dp))
                    AmountField(
                        value = state.monthlyRent,
                        onValueChange = { vm.update { copy(monthlyRent = it) } },
                        label = "Monthly Rent *",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AmountField(
                        value = state.taxAmount,
                        onValueChange = { vm.update { copy(taxAmount = it) } },
                        label = "Monthly Tax",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AmountField(
                        value = state.securityDeposit,
                        onValueChange = { vm.update { copy(securityDeposit = it) } },
                        label = "Security Deposit",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.agreementDocPath,
                        onValueChange = { vm.update { copy(agreementDocPath = it) } },
                        label = { Text("Agreement Document Path (optional)") },
                        leadingIcon = { Icon(Icons.Default.AttachFile, null, tint = NeutralGray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (error != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { vm.save() },
                    enabled = !isSaving && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (isEdit) "Update" else "Save Tenant & Agreement",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FormSectionHeader(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Green800, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = NeutralGray)
    }
}
