package com.santhomach.commercialledger.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AmountFormatTest {

    @Test
    fun formatAmount_usesIndianGrouping() {
        assertEquals("₹0.00", formatAmount(0))
        assertEquals("₹0.05", formatAmount(5))
        assertEquals("₹999.00", formatAmount(99_900))
        assertEquals("₹1,000.00", formatAmount(100_000))
        assertEquals("₹12,345.60", formatAmount(1_234_560))
        assertEquals("₹1,00,000.00", formatAmount(10_000_000))
        assertEquals("₹12,34,567.89", formatAmount(123_456_789))
        assertEquals("₹1,23,45,67,890.00", formatAmount(123_456_789_000))
    }

    @Test
    fun formatAmount_negativeHasLeadingSign() {
        assertEquals("-₹1,500.25", formatAmount(-150_025))
    }

    @Test
    fun displayString_roundTripsThroughParse_inCommaDecimalLocale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            for (paise in listOf(0L, 5L, 150_025L, 10_000_000L)) {
                val shown = paiToDisplayString(paise)
                assertEquals(paise, parseAmountToPaise(shown))
            }
            assertEquals("1500.25", paiToDisplayString(150_025))
        } finally {
            Locale.setDefault(original)
        }
    }

    @Test
    fun monthName_isCapitalised() {
        assertEquals("January", monthName(1))
        assertEquals("September", monthName(9))
    }
}
