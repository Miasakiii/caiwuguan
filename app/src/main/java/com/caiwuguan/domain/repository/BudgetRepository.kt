package com.caiwuguan.domain.repository

import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.Budget
import com.caiwuguan.domain.model.Ledger
import com.caiwuguan.domain.model.MerchantCategory
import com.caiwuguan.domain.model.MonthlyStats
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAll(): Flow<List<Budget>>
    fun getByPeriod(year: Int, month: Int): Flow<List<Budget>>
    suspend fun insert(budget: Budget): Long
    suspend fun update(budget: Budget)
    suspend fun delete(budget: Budget)
}

interface LedgerRepository {
    fun getAll(): Flow<List<Ledger>>
    suspend fun getDefault(): Ledger?
    suspend fun insert(ledger: Ledger): Long
    suspend fun clearDefaults()
    suspend fun deleteById(id: Long)
}

interface MerchantCategoryRepository {
    fun getRecent(limit: Int = 20): Flow<List<MerchantCategory>>
    suspend fun insert(merchantCategory: MerchantCategory): Long
    suspend fun getFavoriteCategory(merchant: String): String?
    suspend fun deleteByMerchant(merchant: String)
}

interface MonthlyStatsRepository {
    fun getAll(): Flow<List<MonthlyStats>>
    fun getByYearMonth(year: Int, month: Int): Flow<MonthlyStats?>
    suspend fun insert(stats: MonthlyStats): Long
}
