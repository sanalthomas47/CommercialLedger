package com.commercialledger.ui.screens.complex

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.commercialledger.CommercialLedgerApplication
import com.commercialledger.data.model.TenancyStatus
import com.commercialledger.ui.components.DoorCard
import com.commercialledger.ui.components.formatAmount
import com.commercialledger.ui.navigation.Screen
import com.commercialledger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplexDetailScreen(complexId: Long, navController: NavHostController) {
    val app = LocalContext.current.applicationContext as CommercialLedgerApplication
    val vm: ComplexDetailViewModel = viewModel(
        factory = ComplexDetailViewModel.factory(complexId, app.container.repository)
    )

    val complex by vm.complex.collectAsState()
    val rooms by vm.rooms.collectAsState()
    val activeTenancyMap by vm.activeTenancyMap.collectAsState()
    val expectedRent by vm.expectedMonthlyRent.collectAsState()
    val collectedMonth by vm.collectedThisMonth.collectAsState()
    val expenses by vm.complexExpenses.collectAsState()

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val occupiedCount = rooms.count { activeTenancyMap[it.id]?.status == TenancyStatus.ACTIVE }
    val collectionRate = if (expectedRent > 0) (collectedMonth.toFloat() / expectedRent.toFloat()).coerceIn(0f, 1f) else 0f

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddRoomDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Door") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 88.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Teal900, Teal700, Green700),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(top = 48.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                Column {
                                    Text(
                                        complex?.name ?: "Complex",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (complex?.address?.isNotBlank() == true) {
                                        Text(
                                            complex!!.address,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = { navController.navigate(Screen.ExpenseForm.createRoute(complexId)) }
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    contentDescription = "Add Expense",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    GlassMetric("Expected/mo", expectedRent)
                                    Box(Modifier.width(1.dp).height(48.dp).background(Color.White.copy(alpha = 0.3f)))
                                    GlassMetric("Collected", collectedMonth)
                                    Box(Modifier.width(1.dp).height(48.dp).background(Color.White.copy(alpha = 0.3f)))
                                    GlassMetricText("Occupied", "$occupiedCount / ${rooms.size}")
                                }
                                if (expectedRent > 0) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "Collection rate",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.75f)
                                        )
                                        Text(
                                            "${(collectionRate * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(collectionRate)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Amber500)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (rooms.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MeetingRoom, null, modifier = Modifier.size(72.dp), tint = Green200)
                        Spacer(Modifier.height(16.dp))
                        Text("No doors yet", style = MaterialTheme.typography.titleMedium, color = NeutralGray)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap the button below to add your first door",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Doors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = CircleShape, color = Green800) {
                            Text(
                                "${rooms.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                itemsIndexed(rooms, key = { _, r -> r.id }) { _, room ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically { it / 2 }
                    ) {
                        val tenancy = activeTenancyMap[room.id]
                        DoorCard(
                            room = room,
                            tenancyStatus = tenancy?.status,
                            tenantName = null,
                            monthlyRent = tenancy?.monthlyRent ?: 0L,
                            onClick = { navController.navigate(Screen.DoorDetail.createRoute(room.id)) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            if (expenses.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Complex Expenses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                itemsIndexed(expenses, key = { _, e -> e.id }) { _, expense ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ExpenseRed.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        expense.category.ifBlank { "Expense" },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(expense.date, style = MaterialTheme.typography.bodySmall, color = NeutralGray)
                                    if (expense.description.isNotBlank()) {
                                        Text(
                                            expense.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NeutralGray.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                            Text(
                                formatAmount(expense.amount),
                                style = MaterialTheme.typography.titleSmall,
                                color = ExpenseRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            onDismiss = { showAddRoomDialog = false },
            onConfirm = { doorNumber, floor, description ->
                vm.addRoom(doorNumber, floor, description)
                showAddRoomDialog = false
            }
        )
    }
}

@Composable
private fun GlassMetric(label: String, paise: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            formatAmount(paise),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.75f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun GlassMetricText(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.75f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var doorNumber by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MeetingRoom, null, tint = Green800)
                Spacer(Modifier.width(8.dp))
                Text("Add Door")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    doorNumber, { doorNumber = it },
                    label = { Text("Door Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    floor, { floor = it },
                    label = { Text("Floor (e.g. Ground, 1st)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    description, { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (doorNumber.isNotBlank()) onConfirm(doorNumber.trim(), floor.trim(), description.trim()) },
                enabled = doorNumber.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
