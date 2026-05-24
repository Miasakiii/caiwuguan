package com.caiwuguan.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_stats")
data class MonthlyStatsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val year: Int,
    val month: Int,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val billCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
