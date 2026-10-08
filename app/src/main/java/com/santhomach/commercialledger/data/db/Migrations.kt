package com.santhomach.commercialledger.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    // v1 → v2: expenses.roomId gains a FOREIGN KEY to room_units (ON DELETE CASCADE),
    // so deleting a door also deletes its expenses. SQLite cannot add a foreign key
    // to an existing table, so the table is rebuilt. The CREATE statements must match
    // schemas/…/2.json exactly or Room's schema validation fails on open.
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Expenses left behind by doors deleted under v1 would violate the new key.
            db.execSQL(
                "DELETE FROM expenses WHERE roomId IS NOT NULL " +
                    "AND roomId NOT IN (SELECT id FROM room_units)"
            )

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `expenses_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`complexId` INTEGER NOT NULL, `roomId` INTEGER, `amount` INTEGER NOT NULL, " +
                    "`date` TEXT NOT NULL, `month` INTEGER NOT NULL, `year` INTEGER NOT NULL, " +
                    "`category` TEXT NOT NULL, `description` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                    "`createdAt` TEXT NOT NULL, " +
                    "FOREIGN KEY(`complexId`) REFERENCES `complexes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                    "FOREIGN KEY(`roomId`) REFERENCES `room_units`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
            )
            // The type column follows roomId (v1 allowed DOOR with no door)
            db.execSQL(
                "INSERT INTO `expenses_new` (id, complexId, roomId, amount, date, month, year, " +
                    "category, description, type, createdAt) " +
                    "SELECT id, complexId, roomId, amount, date, month, year, category, description, " +
                    "CASE WHEN roomId IS NULL THEN 'COMPLEX' ELSE 'DOOR' END, createdAt FROM expenses"
            )
            db.execSQL("DROP TABLE expenses")
            db.execSQL("ALTER TABLE expenses_new RENAME TO expenses")

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_complexId` ON `expenses` (`complexId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_roomId` ON `expenses` (`roomId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_year_month` ON `expenses` (`year`, `month`)")
        }
    }
}
