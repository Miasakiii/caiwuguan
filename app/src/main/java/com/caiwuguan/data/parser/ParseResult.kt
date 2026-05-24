package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource

sealed class ParseResult {
    data class Success(
        val amount: Long,
        val type: BillType,
        val merchant: String = "",
        val category: Category = Category.OTHER,
        val transactionId: String? = null,
        val source: PaymentSource,
        val confidence: Float = 1.0f
    ) : ParseResult()

    data class Failure(val reason: String) : ParseResult()
    data object Ignore : ParseResult()
}
