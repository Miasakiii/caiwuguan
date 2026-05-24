package com.caiwuguan.ai

import com.caiwuguan.domain.model.Bill
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 分析助手（预留接口，当前为规则引擎）
 * 后续可接入大模型 API 实现智能分析
 */
@Singleton
class AiHelper @Inject constructor() {

    /**
     * 根据账单历史生成个性化分类建议
     */
    fun suggestCategory(merchant: String, history: List<Bill>): com.caiwuguan.domain.model.Category {
        // 当前使用规则匹配，后续可接入 AI
        return com.caiwuguan.data.parser.CategoryClassifier().classify(merchant).first
    }

    /**
     * 生成月度消费分析报告（占位实现）
     */
    fun generateMonthlyReport(bills: List<Bill>, month: String): String {
        val totalExpense = bills.filter { it.type == com.caiwuguan.domain.model.BillType.EXPENSE }.sumOf { it.amount }
        val totalIncome = bills.filter { it.type == com.caiwuguan.domain.model.BillType.INCOME }.sumOf { it.amount }
        val expenseCount = bills.count { it.type == com.caiwuguan.domain.model.BillType.EXPENSE }

        return """
        ## $month 消费报告
            
        📊 收支概况
        - 总收入：${formatAmount(totalIncome)} 元
        - 总支出：${formatAmount(totalExpense)} 元
        - 净收支：${formatAmount(totalIncome - totalExpense)} 元
        - 交易笔数：$expenseCount 笔
            
        💡 提示：后续将接入 AI 模型，提供更深入的分析和建议。
        """.trimIndent()
    }

    /**
     * 检测异常支出
     */
    fun detectAnomalies(bills: List<Bill>, thresholdMultiplier: Double = 3.0): List<Bill> {
        val expenses = bills.filter { it.type == com.caiwuguan.domain.model.BillType.EXPENSE }
        if (expenses.isEmpty()) return emptyList()

        val avgAmount = expenses.sumOf { it.amount } / expenses.size.toDouble()
        val threshold = avgAmount * thresholdMultiplier

        return expenses.filter { it.amount > threshold }
    }

    private fun formatAmount(amountInCents: Long): String {
        return "%.2f".format(amountInCents / 100.0)
    }
}
