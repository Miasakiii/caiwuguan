package com.caiwuguan.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caiwuguan.data.db.entity.MonthlyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: MonthlyStatsEntity): Long

    @Query("SELECT * FROM monthly_stats WHERE year = :year AND month = :month")
    fun getByYearMonth(year: Int, month: Int): Flow<MonthlyStatsEntity?>

    @Query("SELECT * FROM monthly_stats ORDER BY year DESC, month DESC")
    fun getAll(): Flow<List<MonthlyStatsEntity>>
}
