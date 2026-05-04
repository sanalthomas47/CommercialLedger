package com.commercialledger

import android.app.Application
import com.commercialledger.di.AppContainer

class CommercialLedgerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
