package com.santhomach.commercialledger.ui.components

/** 9 → "September" */
fun monthName(month: Int): String =
    java.time.Month.of(month).name.lowercase().replaceFirstChar { it.uppercase() }
