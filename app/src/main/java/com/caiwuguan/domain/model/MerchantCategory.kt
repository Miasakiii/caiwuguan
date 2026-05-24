package com.caiwuguan.domain.model

data class MerchantCategory(
    val id: Long = 0,
    val merchant: String = "",
    val category: Category = Category.OTHER,
    val learnedAt: Long = System.currentTimeMillis()
)
