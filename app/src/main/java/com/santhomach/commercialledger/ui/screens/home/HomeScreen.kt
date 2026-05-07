package com.santhomach.commercialledger.ui.screens.home

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
import com.santhomach.commercialledger.CommercialLedgerApplication
import com.santhomach.commercialledger.data.model.Complex
import com.santhomach.commercialledger.ui.components.formatAmount
import com.santhomach.commercialledger.ui.navigation.Screen
import com.santhomach.commercialledger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as CommercialLedgerApplication
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app.container.repository))

    val complexes by vm.complexes.collectAsState()
    val thisMonth by vm.totalThisMonth.collectAsState()
    val thisYear by vm.totalThisYear.collectAsState()
    val allTime by vm.totalAllTime.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var expandedMenuId by remember { mutableStateOf<Long?>(null) }
    var complexToEdit by remember { mutableStateOf<Complex?>(null) }
    var complexToDelete by remember { mutableStateOf<Complex?>(null) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Complex") },
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
                                colors = listOf(Green900, Green700, Teal700),
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
                            Column {
                                Text(
                                    "Commercial Ledger",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Rent & Expense Tracker",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                            Row {
                                IconButton(onClick = { navController.navigate(Screen.Summary.route) }) {
                                    Icon(
                                        Icons.Default.BarChart,
                                        contentDescription = "Summary",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                IconButton(onClick = { navController.navigate(Screen.Backup.route) }) {
                                    Icon(
                                        Icons.Default.Backup,
                                        contentDescription = "Backup & Restore",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                GlassMetric("This Month", thisMonth)
                                Box(
                                    Modifier.width(1.dp).height(48.dp)
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                                GlassMetric("This Year", thisYear)
                                Box(
                                    Modifier.width(1.dp).height(48.dp)
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                                GlassMetric("All Time", allTime, Amber500)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "My Complexes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (complexes.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = CircleShape, color = Green800) {
                            Text(
                                "${complexes.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            if (complexes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Business, null,
                            modifier = Modifier.size(72.dp), tint = Green200
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No complexes yet", style = MaterialTheme.typography.titleMedium, color = NeutralGray)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap the button below to add your first complex",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralGray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(complexes) { index, complex ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically { it / 2 }
                    ) {
                        val accentColors = listOf(Green700, Teal700, Green800, Green900)
                        Card(
                            onClick = { navController.navigate(Screen.ComplexDetail.createRoute(complex.id)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .heightIn(min = 64.dp)
                                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                                        .background(accentColors[index % accentColors.size])
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Green50),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                complex.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Green800,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                complex.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (complex.address.isNotBlank()) {
                                                Text(
                                                    complex.address,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = NeutralGray
                                                )
                                            }
                                        }
                                    }
                                    Box {
                                        IconButton(onClick = { expandedMenuId = complex.id }) {
                                            Icon(Icons.Default.MoreVert, null, tint = NeutralGray)
                                        }
                                        DropdownMenu(
                                            expanded = expandedMenuId == complex.id,
                                            onDismissRequest = { expandedMenuId = null }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Edit") },
                                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                                onClick = { complexToEdit = complex; expandedMenuId = null }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete") },
                                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                                onClick = { complexToDelete = complex; expandedMenuId = null }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showAddDialog) {
        AddComplexDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, address, description ->
                vm.addComplex(name, address, description)
                showAddDialog = false
            }
        )
    }

    complexToEdit?.let { complex ->
        EditComplexDialog(
            complex = complex,
            onDismiss = { complexToEdit = null },
            onConfirm = { name, address, description ->
                vm.updateComplex(complex.copy(name = name, address = address, description = description))
                complexToEdit = null
            }
        )
    }

    complexToDelete?.let { complex ->
        AlertDialog(
            onDismissRequest = { complexToDelete = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Complex") },
            text = { Text("Delete \"${complex.name}\"? All doors, tenancies, payments and expenses will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = { vm.deleteComplex(complex); complexToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { complexToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun GlassMetric(label: String, paise: Long, valueColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            formatAmount(paise),
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.75f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun EditComplexDialog(
    complex: Complex,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(complex.name) }
    var address by remember { mutableStateOf(complex.address) }
    var description by remember { mutableStateOf(complex.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, null, tint = Green800)
                Spacer(Modifier.width(8.dp))
                Text("Edit Complex")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Complex Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name.trim(), address.trim(), description.trim()) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddComplexDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, null, tint = Green800)
                Spacer(Modifier.width(8.dp))
                Text("Add Complex")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Complex Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name.trim(), address.trim(), description.trim()) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
