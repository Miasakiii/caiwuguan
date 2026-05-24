package com.caiwuguan.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_categories")
data class MerchantCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchant: String,
    val category: String,
    val learnedAt: Long = System.currentTimeMillis()
)
