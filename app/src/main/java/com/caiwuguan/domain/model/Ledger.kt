package com.caiwuguan.domain.model

data class Ledger(
    val id: Long = 0,
    val name: String = "默认账本",
    val isDefault: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
