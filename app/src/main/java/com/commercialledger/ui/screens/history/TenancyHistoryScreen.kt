package com.commercialledger.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.commercialledger.ui.components.formatAmount
import com.commercialledger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenancyHistoryScreen(roomId: Long, navController: NavHostController) {
    val app = LocalContext.current.applicationContext as CommercialLedgerApplication
    val vm: TenancyHistoryViewModel = viewModel(
        factory = TenancyHistoryViewModel.factory(roomId, app.container.repository)
    )

    val room by vm.room.collectAsState()
    val tenancies by vm.tenancies.collectAsState()
    val tenantMap by vm.tenantMap.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Door ${room?.doorNumber ?: ""} History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (tenancies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(72.dp), tint = Green200)
                    Spacer(Modifier.height(16.dp))
                    Text("No history yet", style = MaterialTheme.typography.titleMedium, color = NeutralGray)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tenant history will appear here once tenancies are added",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralGray.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = Green800, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${tenancies.size} tenancies recorded",
                            style = MaterialTheme.typography.titleSmall,
                            color = NeutralGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                itemsIndexed(tenancies, key = { _, t -> t.id }) { index, tenancy ->
                    val tenant = tenantMap[tenancy.tenantId]
                    val isActive = tenancy.status == TenancyStatus.ACTIVE
                    val isLast = index == tenancies.lastIndex

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) Green700 else NeutralGray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isActive) Icons.Default.Person else Icons.Default.PersonOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (!isLast) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(NeutralGray.copy(alpha = 0.2f))
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = if (isLast) 0.dp else 10.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) OccupiedBg else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(if (isActive) 2.dp else 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
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
                                                .background(if (isActive) Green800.copy(alpha = 0.12f) else NeutralGray.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                (tenant?.name ?: "?").take(1).uppercase(),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = if (isActive) Green800 else NeutralGray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                tenant?.name ?: "Unknown Tenant",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (tenant?.phone?.isNotBlank() == true) {
                                                Text(
                                                    tenant.phone,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = NeutralGray
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        color = if (isActive) Green700.copy(alpha = 0.12f) else NeutralGray.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            if (isActive) "ACTIVE" else "CLOSED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isActive) Green700 else NeutralGray,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = NeutralGray.copy(alpha = 0.15f))
                                Spacer(Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    InfoChip(
                                        label = "From",
                                        value = tenancy.startDate,
                                        tint = Green700
                                    )
                                    InfoChip(
                                        label = "To",
                                        value = if (tenancy.endDate.isNullOrBlank()) "Present" else tenancy.endDate,
                                        tint = if (isActive) NeutralGray else ExpenseRed
                                    )
                                }

                                Spacer(Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    InfoChip(label = "Rent/mo", value = formatAmount(tenancy.monthlyRent), tint = IncomeGreen)
                                    if (tenancy.securityDeposit > 0) {
                                        InfoChip(label = "Deposit", value = formatAmount(tenancy.securityDeposit), tint = Amber700)
                                    }
                                }

                                if (!isActive) {
                                    if (!tenancy.closureNotes.isNullOrBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.AutoMirrored.Filled.Notes, null, tint = NeutralGray, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                tenancy.closureNotes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = NeutralGray
                                            )
                                        }
                                    }
                                    if (tenancy.refundAmount != null && tenancy.refundAmount > 0) {
                                        Spacer(Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.AutoMirrored.Filled.Undo, null, tint = Amber700, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "Refund: ${formatAmount(tenancy.refundAmount)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Amber700,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, tint: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = NeutralGray, fontSize = 10.sp)
        Text(value, style = MaterialTheme.typography.bodySmall, color = tint, fontWeight = FontWeight.SemiBold)
    }
}
