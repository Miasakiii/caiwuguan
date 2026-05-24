package com.caiwuguan.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.caiwuguan.data.db.entity.BillEntity
import kotlinx.coroutines.flow.Flow

data class CategoryAmount(
    val category: String,
    val total: Long
)

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bill: BillEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(bills: List<BillEntity>): List<Long>

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("SELECT * FROM bills WHERE id = :id")
    fun getById(id: Long): Flow<BillEntity?>

    @Query("SELECT * FROM bills WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 5): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE source = :source AND transactionId = :transactionId LIMIT 1")
    suspend fun findByTransactionId(source: String, transactionId: String): BillEntity?

    @Query(
        "SELECT * FROM bills " +
        "WHERE source = :source AND amount = :amount AND merchant = :merchant " +
        "AND timestamp BETWEEN :timeStart AND :timeEnd LIMIT 1"
    )
    suspend fun findDuplicate(
        source: String, amount: Long, merchant: String,
        timeStart: Long, timeEnd: Long
    ): BillEntity?

    @Query("SELECT SUM(amount) FROM bills WHERE type = 'EXPENSE' AND timestamp BETWEEN :start AND :end")
    fun getTotalExpense(start: Long, end: Long): Flow<Long?>

    @Query("SELECT SUM(amount) FROM bills WHERE type = 'INCOME' AND timestamp BETWEEN :start AND :end")
    fun getTotalIncome(start: Long, end: Long): Flow<Long?>

    @Query(
        "SELECT category, SUM(amount) as total FROM bills " +
        "WHERE type = 'EXPENSE' AND timestamp BETWEEN :start AND :end " +
        "GROUP BY category ORDER BY total DESC"
    )
    fun getCategoryBreakdown(start: Long, end: Long): Flow<List<CategoryAmount>>

    @Query(
        "SELECT * FROM bills " +
        "WHERE merchant LIKE '%' || :keyword || '%' " +
        "OR description LIKE '%' || :keyword || '%' " +
        "ORDER BY timestamp DESC"
    )
    fun search(keyword: String): Flow<List<BillEntity>>
}
