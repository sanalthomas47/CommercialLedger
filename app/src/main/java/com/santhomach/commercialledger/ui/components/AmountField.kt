package com.santhomach.commercialledger.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            if (new.isEmpty() || new.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                onValueChange(new)
            }
        },
        label = { Text(label) },
        prefix = { Text("₹") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = true,
        modifier = modifier
    )
}

fun formatAmount(paise: Long): String = "₹%.2f".format(paise / 100.0)

fun parseAmountToPaise(input: String): Long {
    return input.toBigDecimalOrNull()
        ?.multiply(java.math.BigDecimal(100))
        ?.toLong() ?: 0L
}

fun paiToDisplayString(paise: Long): String = "%.2f".format(paise / 100.0)
