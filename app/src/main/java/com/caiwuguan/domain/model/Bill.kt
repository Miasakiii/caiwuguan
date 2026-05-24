package com.caiwuguan.domain.model

data class Bill(
    val id: Long = 0,
    val amount: Long,
    val type: BillType,
    val category: Category = Category.OTHER,
    val merchant: String = "",
    val description: String = "",
    val source: PaymentSource = PaymentSource.OTHER,
    val transactionId: String? = null,
    val notificationText: String? = null,
    val isAutoRecorded: Boolean = false,
    val timestamp: Long,
    val createdAt: Long = System.currentTimeMillis()
)
