package com.caiwuguan.domain.model

data class Budget(
    val id: Long = 0,
    val category: Category = Category.OTHER,
    val amount: Long,
    val period: String = "MONTHLY",
    val year: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val month: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
    val createdAt: Long = System.currentTimeMillis()
)
