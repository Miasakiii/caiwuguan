package com.caiwuguan.domain.model

data class MonthlyStats(
    val id: Long = 0,
    val year: Int,
    val month: Int,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val billCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
