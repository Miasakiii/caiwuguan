package com.caiwuguan.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caiwuguan.data.db.entity.LedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ledger: LedgerEntity): Long

    @Query("SELECT * FROM ledgers ORDER BY createdAt")
    fun getAll(): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledgers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): LedgerEntity?

    @Query("UPDATE ledgers SET isDefault = 0")
    suspend fun clearDefaults()

    @Query("DELETE FROM ledgers WHERE id = :id")
    suspend fun deleteById(id: Long)
}
