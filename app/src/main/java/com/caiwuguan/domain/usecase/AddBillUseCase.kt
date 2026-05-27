package com.caiwuguan.domain.usecase

import com.caiwuguan.ai.AiHelper
import com.caiwuguan.data.parser.CategoryClassifier
import com.caiwuguan.data.parser.ParseResult
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.Deduplicator
import com.caiwuguan.domain.model.DuplicateCheckResult
import com.caiwuguan.domain.model.MerchantCategory
import com.caiwuguan.domain.repository.BillRepository
import com.caiwuguan.domain.repository.MerchantCategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddBillUseCase @Inject constructor(
    private val billRepository: BillRepository,
    private val merchantCategoryRepository: MerchantCategoryRepository,
    private val categoryClassifier: CategoryClassifier,
    private val aiHelper: AiHelper,
    private val deduplicator: Deduplicator
) {

    /**
     * 从通知解析结果创建账单
     */
    suspend fun executeFromParse(
        parseResult: ParseResult.Success,
        notificationText: String,
        packageName: String
    ): AddBillResult {
        // 1. 去重检查
        val checkResult = deduplicator.check(BuildTemporaryBill(parseResult, notificationText))
        if (checkResult is DuplicateCheckResult.Duplicate) {
            return AddBillResult.Duplicate(checkResult.existingBillId)
        }

        // 2. 商户分类学习
        val merchant = parseResult.merchant
        val suggestedCategory = if (merchant.isNotBlank()) {
            val existing = merchantCategoryRepository.getFavoriteCategory(merchant)
            if (existing != null) {
                Category.valueOf(existing)
            } else {
                val cat = aiHelper.suggestCategory(merchant)
                merchantCategoryRepository.insert(
                    MerchantCategory(merchant = merchant, category = cat)
                )
                cat
            }
        } else {
            Category.OTHER
        }

        // 3. 插入账单
        val bill = Bill(
            amount = parseResult.amount,
            type = parseResult.type,
            category = suggestedCategory,
            merchant = merchant,
            description = "",
            source = parseResult.source,
            transactionId = parseResult.transactionId,
            notificationText = notificationText,
            isAutoRecorded = true,
            timestamp = System.currentTimeMillis()
        )

        val billId = billRepository.insertBill(bill)
        return AddBillResult.Success(billId)
    }

    /**
     * 手动添加账单
     */
    suspend fun executeManual(
        amount: Long,
        type: BillType,
        category: Category,
        merchant: String,
        description: String,
        source: com.caiwuguan.domain.model.PaymentSource
    ): AddBillResult {
        val bill = Bill(
            amount = amount,
            type = type,
            category = category,
            merchant = merchant,
            description = description,
            source = source,
            isAutoRecorded = false,
            timestamp = System.currentTimeMillis()
        )

        val billId = billRepository.insertBill(bill)
        return AddBillResult.Success(billId)
    }

    private fun BuildTemporaryBill(
        parseResult: ParseResult.Success,
        notificationText: String
    ): Bill {
        return Bill(
            amount = parseResult.amount,
            type = parseResult.type,
            category = parseResult.category,
            merchant = parseResult.merchant,
            description = "",
            source = parseResult.source,
            transactionId = parseResult.transactionId,
            notificationText = notificationText,
            isAutoRecorded = true,
            timestamp = System.currentTimeMillis()
        )
    }
}

sealed class AddBillResult {
    data class Success(val billId: Long) : AddBillResult()
    data class Duplicate(val existingBillId: Long?) : AddBillResult()
}
