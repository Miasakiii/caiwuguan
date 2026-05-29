package com.caiwuguan.data.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.caiwuguan.data.db.dao.BillDao
import com.caiwuguan.data.db.dao.BudgetDao
import com.caiwuguan.data.db.dao.ChatDao
import com.caiwuguan.data.db.dao.LedgerDao
import com.caiwuguan.data.db.dao.MerchantCategoryDao
import com.caiwuguan.data.db.dao.MonthlyStatsDao
import com.caiwuguan.data.db.entity.BillEntity
import com.caiwuguan.data.db.entity.BudgetEntity
import com.caiwuguan.data.db.entity.ChatConversationEntity
import com.caiwuguan.data.db.entity.ChatMessageEntity
import com.caiwuguan.data.db.entity.LedgerEntity
import com.caiwuguan.data.db.entity.MerchantCategoryEntity
import com.caiwuguan.data.db.entity.MonthlyStatsEntity
import com.caiwuguan.data.db.migration.MIGRATION_2_3
import com.caiwuguan.data.db.migration.MIGRATION_3_4

@Database(
    entities = [
        BillEntity::class,
        BudgetEntity::class,
        LedgerEntity::class,
        MerchantCategoryEntity::class,
        MonthlyStatsEntity::class,
        ChatConversationEntity::class,
        ChatMessageEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun billDao(): BillDao
    abstract fun budgetDao(): BudgetDao
    abstract fun chatDao(): ChatDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun merchantCategoryDao(): MerchantCategoryDao
    abstract fun monthlyStatsDao(): MonthlyStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "caiwuguan_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
