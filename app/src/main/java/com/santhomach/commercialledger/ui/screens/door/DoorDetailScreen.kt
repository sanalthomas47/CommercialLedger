package com.santhomach.commercialledger.ui.screens.door

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.santhomach.commercialledger.CommercialLedgerApplication
import com.santhomach.commercialledger.data.model.Expense
import com.santhomach.commercialledger.data.model.RentPayment
import com.santhomach.commercialledger.data.model.RoomUnit
import com.santhomach.commercialledger.data.model.TenancyStatus
import com.santhomach.commercialledger.ui.components.DatePickerField
import com.santhomach.commercialledger.ui.components.TenancyStatusChip
import com.santhomach.commercialledger.ui.components.formatAmount
import com.santhomach.commercialledger.ui.components.paiToDisplayString
import com.santhomach.commercialledger.ui.navigation.Screen
import com.santhomach.commercialledger.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoorDetailScreen(doorId: Long, navController: NavHostController) {
    val app = LocalContext.current.applicationContext as CommercialLedgerApplication
    val vm: DoorDetailViewModel = viewModel(
        factory = DoorDetailViewModel.factory(doorId, app.container.repository)
    )

    val room by vm.roomState.collectAsState()
    val tenancy by vm.activeTenancy.collectAsState()
    val tenant by vm.activeTenant.collectAsState()
    val tenantAllDoors by vm.tenantAllDoors.collectAsState()
    val payments by vm.recentPayments.collectAsState()
    val doorExpenses by vm.doorExpenses.collectAsState()
    val error by vm.error.collectAsState()
    val isProcessing by vm.isProcessing.collectAsState()
    val successMessage by vm.successMessage.collectAsState()

    val deleted by vm.deleted.collectAsState()
    LaunchedEffect(deleted) { if (deleted) navController.popBackStack() }

    var showPaymentSheet by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var showEditRoomDialog by remember { mutableStateOf(false) }
    var showDeleteRoomDialog by remember { mutableStateOf(false) }
    var showRoomMenu by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<RentPayment?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Door ${room?.doorNumber ?: ""}", fontWeight = FontWeight.SemiBold)
                        if (room?.floor?.isNotBlank() == true)
                            Text(room!!.floor, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.75f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.TenancyHistory.createRoute(doorId)) }) {
                        Icon(Icons.Default.History, "History")
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.ExpenseForm.createRoute(room?.complexId ?: 0L, doorId))
                    }) {
                        Icon(Icons.Default.AttachMoney, "Add Expense")
                    }
                    Box {
                        IconButton(onClick = { showRoomMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More options")
                        }
                        DropdownMenu(expanded = showRoomMenu, onDismissRequest = { showRoomMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit Door") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = { showEditRoomDialog = true; showRoomMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Door") },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { showDeleteRoomDialog = true; showRoomMenu = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Tenancy card
            item {
                val isOccupied = tenancy?.status == TenancyStatus.ACTIVE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOccupied) OccupiedBg else VacantBg
                    ),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Current Occupancy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TenancyStatusChip(status = tenancy?.status)
                        }

                        if (!isOccupied) {
                            Spacer(Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.MeetingRoom, null, tint = Orange700, modifier = Modifier.size(36.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Door is Vacant", style = MaterialTheme.typography.titleMedium, color = Orange700)
                                    Text("No active tenant assigned", style = MaterialTheme.typography.bodySmall, color = NeutralGray)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate(Screen.TenancyForm.createRoute(doorId)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Assign Tenant")
                            }
                        } else {
                            Spacer(Modifier.height(12.dp))
                            // Tenant avatar + info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Green800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        (tenant?.name?.take(1) ?: "T").uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        tenant?.name ?: "Unknown",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (tenant?.phone?.isNotBlank() == true) {
                                        Text(tenant!!.phone, style = MaterialTheme.typography.bodySmall, color = NeutralGray)
                                    }
                                    Text("Since ${tenancy!!.startDate}", style = MaterialTheme.typography.bodySmall, color = NeutralGray)
                                    val otherDoors = tenantAllDoors.filter { it != room?.doorNumber }
                                    if (otherDoors.isNotEmpty()) {
                                        Spacer(Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.MeetingRoom,
                                                contentDescription = null,
                                                tint = Teal700,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                "Also in: ${otherDoors.joinToString(", ") { "Door $it" }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Teal700
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = Green200)
                            Spacer(Modifier.height(12.dp))

                            // Financial details row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FinancialChip("Rent", formatAmount(tenancy!!.monthlyRent) + "/mo", Amber700)
                                if (tenancy!!.taxAmount > 0)
                                    FinancialChip("Tax", formatAmount(tenancy!!.taxAmount) + "/mo", NeutralGray)
                                if (tenancy!!.securityDeposit > 0)
                                    FinancialChip("Deposit", formatAmount(tenancy!!.securityDeposit), Teal700)
                            }

                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { navController.navigate(Screen.TenancyForm.createRoute(doorId, tenancy!!.id)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("Edit") }
                                Button(
                                    onClick = { showPaymentSheet = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !isProcessing
                                ) { Text("Record Payment") }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showCloseDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isProcessing,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = !isProcessing).copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Close Tenancy")
                            }
                        }
                    }
                }
            }

            // Payments section
            if (payments.isNotEmpty()) {
                item {
                    SectionHeader("Recent Payments", Icons.Default.Receipt)
                }
                items(payments, key = { it.id }) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Green50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        payment.month.toString(),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Green800,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "${monthName(payment.month)} ${payment.year}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${payment.paymentDate} · ${payment.paymentMode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralGray
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    formatAmount(payment.amountPaid),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { paymentToDelete = payment },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Door expenses
            if (doorExpenses.isNotEmpty()) {
                item { SectionHeader("Door Expenses", Icons.Default.Receipt) }
                items(doorExpenses, key = { it.id }) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    Modifier.size(40.dp).clip(CircleShape).background(Red600.copy(0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(expense.category.ifBlank { "Expense" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(expense.date, style = MaterialTheme.typography.bodySmall, color = NeutralGray)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(formatAmount(expense.amount), style = MaterialTheme.typography.titleMedium, color = ExpenseRed, fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = { navController.navigate(Screen.ExpenseForm.createRoute(expense.complexId, expense.roomId ?: 0L, expense.id)) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = NeutralGray, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { expenseToDelete = expense },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPaymentSheet) {
        RecordPaymentSheet(
            tenancyMonthlyRent = tenancy?.monthlyRent ?: 0L,
            onDismiss = { showPaymentSheet = false },
            onConfirm = { amount, month, year, mode, notes ->
                vm.recordPayment(amount, month, year, mode, notes)
                showPaymentSheet = false
            }
        )
    }

    if (showCloseDialog) {
        CloseTenancyDialog(
            depositAmount = tenancy?.securityDeposit ?: 0L,
            onDismiss = { showCloseDialog = false },
            onConfirm = { endDate, notes, refund ->
                vm.closeTenancy(endDate, notes, refund)
                showCloseDialog = false
            }
        )
    }

    if (showEditRoomDialog) {
        room?.let { r ->
            EditRoomDialog(
                room = r,
                onDismiss = { showEditRoomDialog = false },
                onConfirm = { doorNumber, floor, description ->
                    vm.updateRoom(doorNumber, floor, description)
                    showEditRoomDialog = false
                }
            )
        }
    }

    if (showDeleteRoomDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteRoomDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Door") },
            text = { Text("Delete door \"${room?.doorNumber}\"? All tenancies and expenses will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = { vm.deleteRoom(); showDeleteRoomDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteRoomDialog = false }) { Text("Cancel") } }
        )
    }

    paymentToDelete?.let { payment ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Payment") },
            text = { Text("Delete ${monthName(payment.month)} ${payment.year} payment of ${formatAmount(payment.amountPaid)}?") },
            confirmButton = {
                Button(
                    onClick = { vm.deletePayment(payment); paymentToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { paymentToDelete = null }) { Text("Cancel") } }
        )
    }

    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Expense") },
            text = { Text("Delete this ${expense.category.ifBlank { "expense" }} of ${formatAmount(expense.amount)}?") },
            confirmButton = {
                Button(
                    onClick = { vm.deleteExpense(expense); expenseToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { expenseToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun EditRoomDialog(
    room: RoomUnit,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var doorNumber by remember { mutableStateOf(room.doorNumber) }
    var floor by remember { mutableStateOf(room.floor) }
    var description by remember { mutableStateOf(room.description) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, null, tint = Green800)
                Spacer(Modifier.width(8.dp))
                Text("Edit Door")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(doorNumber, { doorNumber = it }, label = { Text("Door Number *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(floor, { floor = it }, label = { Text("Floor") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (doorNumber.isNotBlank()) onConfirm(doorNumber.trim(), floor.trim(), description.trim()) }, enabled = doorNumber.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun FinancialChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralGray)
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Green700, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = NeutralGray)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPaymentSheet(
    tenancyMonthlyRent: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long, Int, Int, String, String) -> Unit
) {
    val today = LocalDate.now()
    var amount by remember { mutableStateOf(paiToDisplayString(tenancyMonthlyRent)) }
    var month by remember { mutableIntStateOf(today.monthValue) }
    var year by remember { mutableIntStateOf(today.year) }
    var paymentMode by remember { mutableStateOf("Cash") }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, null, tint = Green700, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Record Rent Payment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("₹") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = month.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.let { if (it in 1..12) month = it } },
                    label = { Text("Month (1-12)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = year.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.let { if (it in 2000..2100) year = it } },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            val modes = listOf("Cash", "Bank Transfer", "Cheque", "UPI")
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = paymentMode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    modes.forEach { mode ->
                        DropdownMenuItem(text = { Text(mode) }, onClick = { paymentMode = mode; expanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val paise = (amount.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
                    if (paise > 0) onConfirm(paise, month, year, paymentMode, notes)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Save Payment", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CloseTenancyDialog(
    depositAmount: Long,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit
) {
    var endDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var notes by remember { mutableStateOf("") }
    var refund by remember { mutableStateOf(paiToDisplayString(depositAmount)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Close Tenancy") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DatePickerField(value = endDate, onValueChange = { endDate = it }, label = "End Date", modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Closure Notes") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = refund,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) refund = it },
                    label = { Text("Deposit Refund (₹)") },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(0.08f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Tenancy will be marked closed. A new tenant can be assigned after.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpenseRed,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val refundPaise = (refund.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
                    onConfirm(endDate, notes.trim(), refundPaise)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Close Tenancy") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun monthName(month: Int) = java.time.Month.of(month).name.lowercase().replaceFirstChar { it.uppercase() }
