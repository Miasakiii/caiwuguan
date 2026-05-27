package com.caiwuguan.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 2 to 3
 * - Add composite index [source, transactionId] to bills table for dedup queries
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_bills_source_transactionId` ON `bills` (`source`, `transactionId`)"
        )
    }
}
