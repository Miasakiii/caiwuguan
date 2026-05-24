package com.caiwuguan.data.repository

import com.caiwuguan.data.db.dao.MonthlyStatsDao
import com.caiwuguan.data.db.entity.MonthlyStatsEntity
import com.caiwuguan.domain.model.MonthlyStats
import com.caiwuguan.domain.repository.MonthlyStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthlyStatsRepositoryImpl @Inject constructor(
    private val monthlyStatsDao: MonthlyStatsDao
) : MonthlyStatsRepository {

    override fun getAll(): Flow<List<MonthlyStats>> =
        monthlyStatsDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByYearMonth(year: Int, month: Int): Flow<MonthlyStats?> =
        monthlyStatsDao.getByYearMonth(year, month).map { it?.toDomain() }

    override suspend fun insert(stats: MonthlyStats): Long =
        monthlyStatsDao.insert(stats.toEntity())

    private fun MonthlyStatsEntity.toDomain() = MonthlyStats(
        id = id,
        year = year,
        month = month,
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        billCount = billCount,
        updatedAt = updatedAt
    )

    private fun MonthlyStats.toEntity() = MonthlyStatsEntity(
        id = id,
        year = year,
        month = month,
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        billCount = billCount,
        updatedAt = updatedAt
    )
}
