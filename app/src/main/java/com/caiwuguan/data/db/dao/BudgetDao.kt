package com.caiwuguan.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.caiwuguan.data.db.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month AND category = :category LIMIT 1")
    suspend fun findByPeriod(year: Int, month: Int, category: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month ORDER BY category")
    fun getByPeriod(year: Int, month: Int): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BudgetEntity>>
}
