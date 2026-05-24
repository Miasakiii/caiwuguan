package com.caiwuguan.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val amount: Long,
    val period: String, // "MONTHLY" or "YEARLY"
    val year: Int,
    val month: Int,
    val createdAt: Long = System.currentTimeMillis()
)
