package com.commercialledger.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RentSummaryCard(
    thisMonth: Long,
    thisYear: Long,
    allTime: Long,
    modifier: Modifier = Modifier,
    title: String = "Rent Collected"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric("This Month", thisMonth)
                VerticalDivider(modifier = Modifier.height(40.dp))
                SummaryMetric("This Year", thisYear)
                VerticalDivider(modifier = Modifier.height(40.dp))
                SummaryMetric("All Time", allTime)
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, paise: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatAmount(paise),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
    }
}
