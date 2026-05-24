package com.caiwuguan.data.repository

import com.caiwuguan.data.db.dao.BudgetDao
import com.caiwuguan.data.db.entity.BudgetEntity
import com.caiwuguan.domain.model.Budget
import com.caiwuguan.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getAll(): Flow<List<Budget>> =
        budgetDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByPeriod(year: Int, month: Int): Flow<List<Budget>> =
        budgetDao.getByPeriod(year, month).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(budget: Budget): Long =
        budgetDao.insert(budget.toEntity())

    override suspend fun update(budget: Budget) =
        budgetDao.update(budget.toEntity())

    override suspend fun delete(budget: Budget) =
        budgetDao.delete(budget.toEntity())

    private fun BudgetEntity.toDomain() = Budget(
        id = id,
        category = com.caiwuguan.domain.model.Category.valueOf(category),
        amount = amount,
        period = period,
        year = year,
        month = month,
        createdAt = createdAt
    )

    private fun Budget.toEntity() = BudgetEntity(
        id = id,
        category = category.name,
        amount = amount,
        period = period,
        year = year,
        month = month,
        createdAt = createdAt
    )
}
