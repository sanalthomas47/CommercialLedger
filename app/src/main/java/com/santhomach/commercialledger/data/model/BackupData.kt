package com.santhomach.commercialledger.data.model

// Gson ignores Kotlin defaults and nullability, so every field read back from a
// file may be null; the lists are nullable and LedgerRepository guards them.
data class BackupData(
    val version: Int = 1,
    val exportedAt: String?,
    val complexes: List<Complex>?,
    val roomUnits: List<RoomUnit>?,
    val tenants: List<Tenant>?,
    val tenancies: List<Tenancy>?,
    val rentPayments: List<RentPayment>?,
    val expenses: List<Expense>?,
)
