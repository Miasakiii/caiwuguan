package com.caiwuguan.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caiwuguan.data.db.entity.MerchantCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(merchantCategory: MerchantCategoryEntity): Long

    @Query("SELECT category FROM merchant_categories WHERE merchant = :merchant LIMIT 1")
    suspend fun getFavoriteCategory(merchant: String): String?

    @Query("SELECT * FROM merchant_categories ORDER BY learnedAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 20): Flow<List<MerchantCategoryEntity>>

    @Query("DELETE FROM merchant_categories WHERE merchant = :merchant")
    suspend fun deleteByMerchant(merchant: String)
}
