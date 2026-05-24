package com.caiwuguan.domain.repository

import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface BillRepository {

    fun getAllBills(): Flow<List<Bill>>

    fun getRecentBills(limit: Int = 5): Flow<List<Bill>>

    fun getBillById(id: Long): Flow<Bill?>

    fun getBillsByDateRange(start: Long, end: Long): Flow<List<Bill>>

    fun getTotalExpense(start: Long, end: Long): Flow<Long>

    fun getTotalIncome(start: Long, end: Long): Flow<Long>

    fun getCategoryBreakdown(start: Long, end: Long): Flow<Map<Category, Long>>

    fun search(keyword: String): Flow<List<Bill>>

    suspend fun insertBill(bill: Bill): Long

    suspend fun insertBills(bills: List<Bill>): List<Long>

    suspend fun updateBill(bill: Bill)

    suspend fun deleteBill(bill: Bill)
}
