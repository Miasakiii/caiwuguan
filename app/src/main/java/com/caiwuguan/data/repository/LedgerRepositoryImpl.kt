package com.caiwuguan.data.repository

import com.caiwuguan.data.db.dao.LedgerDao
import com.caiwuguan.data.db.entity.LedgerEntity
import com.caiwuguan.domain.model.Ledger
import com.caiwuguan.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LedgerRepositoryImpl @Inject constructor(
    private val ledgerDao: LedgerDao
) : LedgerRepository {

    override fun getAll(): Flow<List<Ledger>> =
        ledgerDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getDefault(): Ledger? =
        ledgerDao.getDefault()?.toDomain()

    override suspend fun insert(ledger: Ledger): Long =
        ledgerDao.insert(ledger.toEntity())

    override suspend fun clearDefaults() =
        ledgerDao.clearDefaults()

    override suspend fun deleteById(id: Long) =
        ledgerDao.deleteById(id)

    private fun LedgerEntity.toDomain() = Ledger(
        id = id,
        name = name,
        isDefault = isDefault,
        createdAt = createdAt
    )

    private fun Ledger.toEntity() = LedgerEntity(
        id = id,
        name = name,
        isDefault = isDefault,
        createdAt = createdAt
    )
}
