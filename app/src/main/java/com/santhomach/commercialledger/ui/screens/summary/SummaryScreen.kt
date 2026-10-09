package com.santhomach.commercialledger.ui.screens.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.santhomach.commercialledger.ui.components.formatAmount
import com.santhomach.commercialledger.ui.theme.*
import java.time.LocalDate
import java.time.Month

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as CommercialLedgerApplication
    val vm: SummaryViewModel = viewModel(factory = SummaryViewModel.factory(app.container.repository))

    val selectedMonth by vm.selectedMonth.collectAsState()
    val selectedYear by vm.selectedYear.collectAsState()
    val perComplex by vm.perComplexSummary.collectAsState()
    val global by vm.globalSummary.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Green900, Green800, Teal700),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(top = 48.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(Modifier.width(4.dp))
                            Column {
                                Text(
                                    "Financial Summary",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Income & expense overview",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.75f)
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
                                    GlassMetric("This Month", global.totalCollectedThisMonth)
                                    Box(Modifier.width(1.dp).height(48.dp).background(Color.White.copy(alpha = 0.3f)))
                                    GlassMetric("This Year", global.totalCollectedThisYear)
                                    Box(Modifier.width(1.dp).height(48.dp).background(Color.White.copy(alpha = 0.3f)))
                                    GlassMetric("All Time", global.allTimeCollected, Amber500)
                                }

                                if (global.totalCollectedThisYear > 0 || global.totalExpensesThisYear > 0) {
                                    Spacer(Modifier.height(14.dp))
                                    IncomeExpenseBar(
                                        income = global.totalCollectedThisYear,
                                        expenses = global.totalExpensesThisYear
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "Income ${formatAmount(global.totalCollectedThisYear)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "Expenses ${formatAmount(global.totalExpensesThisYear)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Newest first: next year down to ten years back
                    val currentYear = LocalDate.now().year
                    val years = (currentYear + 1 downTo currentYear - 10).toList()
                    var yearExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = it },
                        modifier = Modifier.width(120.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Year") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                            years.forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y.toString()) },
                                    onClick = { vm.selectYear(y); yearExpanded = false }
                                )
                            }
                        }
                    }
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NeutralGray, modifier = Modifier.size(16.dp))
                    Text(
                        Month.of(selectedMonth).name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items((1..12).toList()) { m ->
                        FilterChip(
                            selected = m == selectedMonth,
                            onClick = { vm.selectMonth(m) },
                            label = { Text(Month.of(m).name.take(3)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green800,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = Green800, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Overall Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        SummaryRow("Collected this month", global.totalCollectedThisMonth, IncomeGreen)
                        SummaryRow("Collected this year", global.totalCollectedThisYear, IncomeGreen)
                        SummaryRow("All-time collected", global.allTimeCollected, IncomeGreen)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SummaryRow("Expenses this year", global.totalExpensesThisYear, ExpenseRed)
                        Spacer(Modifier.height(4.dp))
                        val netColor = if (global.netIncomeThisYear >= 0) IncomeGreen else ExpenseRed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(netColor.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Net income this year",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = netColor
                            )
                            Text(
                                formatAmount(global.netIncomeThisYear),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = netColor
                            )
                        }
                    }
                }
            }

            if (perComplex.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "By Complex",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = CircleShape, color = Green800) {
                            Text(
                                "${perComplex.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                items(perComplex, key = { it.complex.id }) { cs ->
                    val collectionRate = if (cs.expectedMonthlyRent > 0)
                        (cs.collectedThisMonth.toFloat() / cs.expectedMonthlyRent.toFloat()).coerceIn(0f, 1f)
                    else 0f
                    val netColor = if (cs.netIncomeThisYear >= 0) IncomeGreen else ExpenseRed

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Green50),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            cs.complex.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Green800,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(cs.complex.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        if (cs.complex.address.isNotBlank()) {
                                            Text(cs.complex.address, style = MaterialTheme.typography.bodySmall, color = NeutralGray)
                                        }
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = netColor.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        formatAmount(cs.netIncomeThisYear),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = netColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (cs.expectedMonthlyRent > 0) {
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Collected ${formatAmount(cs.collectedThisMonth)} of ${formatAmount(cs.expectedMonthlyRent)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralGray
                                    )
                                    Text(
                                        "${(collectionRate * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (collectionRate >= 1f) IncomeGreen else NeutralGray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFE0E0E0))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(collectionRate)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (collectionRate >= 1f) IncomeGreen else Amber500)
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                MiniStat("Year Income", cs.collectedThisYear, IncomeGreen)
                                MiniStat("Expenses", cs.totalExpensesThisYear, ExpenseRed)
                                MiniStat("All Time", cs.allTimeCollected, Amber700)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
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
private fun IncomeExpenseBar(income: Long, expenses: Long) {
    val total = (income + expenses).coerceAtLeast(1L)
    val incomeRatio = income.toFloat() / total.toFloat()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        if (incomeRatio > 0f) {
            Box(modifier = Modifier.weight(incomeRatio).fillMaxHeight().background(IncomeGreen))
        }
        if (incomeRatio < 1f) {
            Box(modifier = Modifier.weight(1f - incomeRatio).fillMaxHeight().background(ExpenseRed.copy(alpha = 0.75f)))
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Long, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = NeutralGray)
        Text(formatAmount(amount), style = MaterialTheme.typography.bodyMedium, color = valueColor, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}

@Composable
private fun MiniStat(label: String, amount: Long, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(formatAmount(amount), style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralGray, textAlign = TextAlign.Center)
    }
}
