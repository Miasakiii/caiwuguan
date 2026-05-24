package com.caiwuguan.data.repository

import com.caiwuguan.data.db.dao.MerchantCategoryDao
import com.caiwuguan.data.db.entity.MerchantCategoryEntity
import com.caiwuguan.domain.model.MerchantCategory
import com.caiwuguan.domain.repository.MerchantCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantCategoryRepositoryImpl @Inject constructor(
    private val merchantCategoryDao: MerchantCategoryDao
) : MerchantCategoryRepository {

    override fun getRecent(limit: Int): Flow<List<MerchantCategory>> =
        merchantCategoryDao.getRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(merchantCategory: MerchantCategory): Long =
        merchantCategoryDao.insert(merchantCategory.toEntity())

    override suspend fun getFavoriteCategory(merchant: String): String? =
        merchantCategoryDao.getFavoriteCategory(merchant)

    override suspend fun deleteByMerchant(merchant: String) =
        merchantCategoryDao.deleteByMerchant(merchant)

    private fun MerchantCategoryEntity.toDomain() = MerchantCategory(
        id = id,
        merchant = merchant,
        category = com.caiwuguan.domain.model.Category.valueOf(category),
        learnedAt = learnedAt
    )

    private fun MerchantCategory.toEntity() = MerchantCategoryEntity(
        id = id,
        merchant = merchant,
        category = category.name,
        learnedAt = learnedAt
    )
}
