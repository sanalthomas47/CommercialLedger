package com.santhomach.commercialledger.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.santhomach.commercialledger.data.dao.*
import com.santhomach.commercialledger.data.model.*

@Database(
    entities = [
        Complex::class,
        RoomUnit::class,
        Tenant::class,
        Tenancy::class,
        RentPayment::class,
        Expense::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun complexDao(): ComplexDao
    abstract fun roomUnitDao(): RoomUnitDao
    abstract fun tenantDao(): TenantDao
    abstract fun tenancyDao(): TenancyDao
    abstract fun rentPaymentDao(): RentPaymentDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        private const val DATABASE_NAME = "commercial_ledger.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // When bumping `version`, add a Migration or @AutoMigration to @Database
                    // instead of fallbackToDestructiveMigration — that wipes all user data.
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
