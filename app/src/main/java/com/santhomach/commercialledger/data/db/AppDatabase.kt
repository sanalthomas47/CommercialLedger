package com.santhomach.commercialledger.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.santhomach.commercialledger.data.dao.*
import com.santhomach.commercialledger.data.model.*

// ════════════════════════════════════════════════════════════════════════════
// HOW TO ADD A NEW FIELD WITHOUT LOSING USER DATA
// ════════════════════════════════════════════════════════════════════════════
//
// Step 1 — Add the field to the entity WITH @ColumnInfo(defaultValue):
//
//   Nullable column      → val foo: String? = null          (no defaultValue needed)
//   NOT NULL String      → @ColumnInfo(defaultValue = "")   val foo: String = ""
//   NOT NULL Int / Long  → @ColumnInfo(defaultValue = "0")  val foo: Int = 0
//   NOT NULL Boolean     → @ColumnInfo(defaultValue = "0")  val foo: Boolean = false
//   Enum (stored as TEXT)→ @ColumnInfo(defaultValue = "ACTIVE") val status: MyEnum = MyEnum.ACTIVE
//
//   ⚠  Skipping @ColumnInfo(defaultValue) on a NOT NULL column will cause a
//      compile-time error from Room — it cannot generate the ALTER TABLE SQL.
//
// Step 2 — Increment `version` in @Database below by 1.
//
// Step 3 — Add an AutoMigration entry (simple column additions are handled
//          automatically; no SQL needed):
//
//   autoMigrations = [
//       AutoMigration(from = 1, to = 2),   // ← add the new pair here
//   ]
//
// Step 4 — Build the project. Room regenerates the schema JSON.
//          Commit the updated JSON file alongside your entity change.
//          Do NOT hand-edit the schema JSON files.
//
// Step 5 (only for complex changes like renames, drops, NOT NULL without a
//          sensible default, or multi-table restructures) — write a manual
//          Migration in Migrations.kt and register it with .addMigrations(…)
//          in getInstance() below instead of (or in addition to) AutoMigration.
//
// NEVER call fallbackToDestructiveMigration() — it silently wipes all data.
// ════════════════════════════════════════════════════════════════════════════

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
    // When adding a new field, increment version above and append the new
    // AutoMigration pair here. See the guide at the top of this file.
    autoMigrations = [
        // AutoMigration(from = 1, to = 2),
    ],
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
                    // Truncate journal mode: single .db file, no WAL accumulation.
                    // Simpler for Android Auto Backup (only the .db file needs restoring).
                    // Safe on existing WAL databases — SQLite checkpoints the WAL and
                    // switches modes automatically on the next open.
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            // Force a full WAL checkpoint on every open.
                            // This merges any WAL data into the main .db file immediately,
                            // so Android Auto Backup always captures a consistent, complete
                            // snapshot — even on the first open after switching from WAL mode.
                            db.execSQL("PRAGMA wal_checkpoint(FULL)")
                        }
                    })
                    // Register manual migrations here for complex changes that
                    // @AutoMigration cannot handle automatically.
                    // .addMigrations(Migrations.MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
