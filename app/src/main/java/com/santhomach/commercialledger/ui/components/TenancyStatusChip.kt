package com.santhomach.commercialledger.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.santhomach.commercialledger.data.model.TenancyStatus
import com.santhomach.commercialledger.ui.theme.Green600
import com.santhomach.commercialledger.ui.theme.Red600

@Composable
fun TenancyStatusChip(status: TenancyStatus?, modifier: Modifier = Modifier) {
    val occupied = status == TenancyStatus.ACTIVE
    val bgColor = if (occupied) Green600.copy(alpha = 0.15f) else Red600.copy(alpha = 0.12f)
    val textColor = if (occupied) Green600 else Red600
    val label = if (occupied) "OCCUPIED" else "VACANT"

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
