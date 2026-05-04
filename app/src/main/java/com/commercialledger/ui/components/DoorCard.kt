package com.commercialledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commercialledger.data.model.RoomUnit
import com.commercialledger.data.model.TenancyStatus
import com.commercialledger.ui.theme.*

@Composable
fun DoorCard(
    room: RoomUnit,
    tenancyStatus: TenancyStatus?,
    tenantName: String?,
    monthlyRent: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOccupied = tenancyStatus == TenancyStatus.ACTIVE
    val accentColor = if (isOccupied) Green700 else Amber700
    val bgColor = if (isOccupied) OccupiedBg else VacantBg

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .heightIn(min = 64.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .background(accentColor)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = room.doorNumber.take(2),
                            style = MaterialTheme.typography.titleSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Door ${room.doorNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (room.floor.isNotBlank()) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "· ${room.floor}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralGray
                                )
                            }
                        }
                        if (!tenantName.isNullOrBlank()) {
                            Text(
                                tenantName,
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralGray
                            )
                        } else if (monthlyRent > 0) {
                            Text(
                                formatAmount(monthlyRent) + "/mo",
                                style = MaterialTheme.typography.bodySmall,
                                color = accentColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                TenancyStatusChip(status = tenancyStatus)
            }
        }
    }
}
