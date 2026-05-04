package com.commercialledger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick date")
            }
        },
        modifier = modifier
    )

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = if (value.isNotEmpty()) {
                runCatching {
                    LocalDate.parse(value, fmt)
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant()
                        .toEpochMilli()
                }.getOrNull()
            } else null
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        onValueChange(date.format(fmt))
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
