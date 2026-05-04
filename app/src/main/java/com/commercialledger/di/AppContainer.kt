package com.commercialledger.di

import android.content.Context
import com.commercialledger.data.db.AppDatabase
import com.commercialledger.data.repository.LedgerRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val repository = LedgerRepository(
        database = database,
        complexDao = database.complexDao(),
        roomUnitDao = database.roomUnitDao(),
        tenantDao = database.tenantDao(),
        tenancyDao = database.tenancyDao(),
        rentPaymentDao = database.rentPaymentDao(),
        expenseDao = database.expenseDao(),
    )
}
