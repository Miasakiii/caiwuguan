package com.caiwuguan.domain.model

import javax.inject.Inject

/**
 * 三级去重引擎
 */
class Deduplicator @Inject constructor(
    private val billDao: com.caiwuguan.data.db.dao.BillDao
) {
    /**
     * 三级去重策略：
     * 1. 精确匹配：source + transactionId → 命中则一定是重复
     * 2. 模糊匹配：source + amount + timestamp(±5min) + merchant → 同源近似
     * 3. 宽松匹配：amount + date + merchant → 跨来源去重
     */
    suspend fun check(bill: Bill): DuplicateCheckResult {
        // 精确匹配
        bill.transactionId?.let { transactionId ->
            val exact = billDao.findByTransactionId(bill.source.name, transactionId)
            if (exact != null) return DuplicateCheckResult.Duplicate(
                DuplicateCheckResult.MatchType.EXACT, exact.id
            )
        }

        // 模糊匹配（±5 分钟）
        val windowMs = 5 * 60 * 1000L
        val fuzzy = billDao.findDuplicate(
            bill.source.name, bill.amount, bill.merchant,
            bill.timestamp - windowMs, bill.timestamp + windowMs
        )
        if (fuzzy != null) return DuplicateCheckResult.Duplicate(
            DuplicateCheckResult.MatchType.FUZZY, fuzzy.id
        )

        // 宽松匹配（同一天，跨来源）
        val dayStart = bill.timestamp - (bill.timestamp % (24 * 60 * 60 * 1000L))
        val dayEnd = dayStart + 24 * 60 * 60 * 1000L
        // 注：BillDao 暂不支持 findLooseDuplicate，后续可添加
        // val loose = billDao.findLooseDuplicate(bill.amount, bill.merchant, dayStart, dayEnd)
        // if (loose != null) return DuplicateCheckResult.Duplicate(DuplicateCheckResult.MatchType.LOOSE, loose.id)

        return DuplicateCheckResult.Unique("未发现重复交易")
    }
}
