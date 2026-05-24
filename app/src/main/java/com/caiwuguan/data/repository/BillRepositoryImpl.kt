package com.caiwuguan.data.repository

import com.caiwuguan.data.db.dao.BillDao
import com.caiwuguan.data.db.entity.BillEntity
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import com.caiwuguan.domain.repository.BillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillRepositoryImpl @Inject constructor(
    private val billDao: BillDao
) : BillRepository {

    override fun getAllBills(): Flow<List<Bill>> =
        billDao.getByDateRange(0, Long.MAX_VALUE).map { list -> list.map { it.toDomain() } }

    override fun getRecentBills(limit: Int): Flow<List<Bill>> =
        billDao.getRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun getBillById(id: Long): Flow<Bill?> =
        billDao.getById(id).map { it?.toDomain() }

    override fun getBillsByDateRange(start: Long, end: Long): Flow<List<Bill>> =
        billDao.getByDateRange(start, end).map { list -> list.map { it.toDomain() } }

    override fun getTotalExpense(start: Long, end: Long): Flow<Long> =
        billDao.getTotalExpense(start, end).map { it ?: 0L }

    override fun getTotalIncome(start: Long, end: Long): Flow<Long> =
        billDao.getTotalIncome(start, end).map { it ?: 0L }

    override fun getCategoryBreakdown(start: Long, end: Long): Flow<Map<Category, Long>> =
        billDao.getCategoryBreakdown(start, end).map { list ->
            list.associate { (categoryStr, total) ->
                val category = Category.entries.find { it.name == categoryStr } ?: Category.OTHER
                category to total
            }
        }

    override fun search(keyword: String): Flow<List<Bill>> =
        billDao.search(keyword).map { list -> list.map { it.toDomain() } }

    override suspend fun insertBill(bill: Bill): Long =
        billDao.insert(bill.toEntity())

    override suspend fun insertBills(bills: List<Bill>): List<Long> =
        billDao.insertAll(bills.map { it.toEntity() })

    override suspend fun updateBill(bill: Bill) =
        billDao.update(bill.toEntity())

    override suspend fun deleteBill(bill: Bill) =
        billDao.delete(bill.toEntity())

    private fun BillEntity.toDomain() = Bill(
        id = id,
        amount = amount,
        type = BillType.valueOf(type),
        category = Category.entries.find { it.name == category } ?: Category.OTHER,
        merchant = merchant,
        description = description,
        source = PaymentSource.entries.find { it.name == source } ?: PaymentSource.OTHER,
        transactionId = transactionId,
        notificationText = notificationText,
        isAutoRecorded = isAutoRecorded,
        timestamp = timestamp,
        createdAt = createdAt
    )

    private fun Bill.toEntity() = BillEntity(
        id = id,
        amount = amount,
        type = type.name,
        category = category.name,
        merchant = merchant,
        description = description,
        source = source.name,
        transactionId = transactionId,
        notificationText = notificationText,
        isAutoRecorded = isAutoRecorded,
        timestamp = timestamp,
        createdAt = createdAt
    )
}
