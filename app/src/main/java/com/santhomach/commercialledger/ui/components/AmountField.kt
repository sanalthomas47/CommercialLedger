package com.santhomach.commercialledger.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import kotlin.math.abs

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

/** ₹ with Indian digit grouping, e.g. 12345678 paise → "₹1,23,456.78"; negatives as "-₹…". */
fun formatAmount(paise: Long): String {
    val sign = if (paise < 0) "-" else ""
    return "$sign₹" + groupIndian(abs(paise) / 100) + "." + (abs(paise) % 100).toString().padStart(2, '0')
}

// Last three digits, then groups of two: 1234567 → "12,34,567"
private fun groupIndian(rupees: Long): String {
    val digits = rupees.toString()
    if (digits.length <= 3) return digits
    val head = digits.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
    return head + "," + digits.takeLast(3)
}

fun parseAmountToPaise(input: String): Long {
    return input.toBigDecimalOrNull()
        ?.multiply(java.math.BigDecimal(100))
        ?.toLong() ?: 0L
}

// Locale-independent (String.format would emit "1234,50" on comma-decimal locales,
// which parseAmountToPaise cannot read back)
fun paiToDisplayString(paise: Long): String {
    val sign = if (paise < 0) "-" else ""
    return sign + (abs(paise) / 100) + "." + (abs(paise) % 100).toString().padStart(2, '0')
}
