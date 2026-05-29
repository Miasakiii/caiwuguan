package com.caiwuguan.ai

import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.repository.BillRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RAG 上下文构建器
 * 将用户的账单数据注入 AI 对话上下文，使 AI 能够基于真实数据回答问题
 */
@Singleton
class ChatContextBuilder @Inject constructor(
    private val billRepository: BillRepository
) {

    companion object {
        private const val SYSTEM_PROMPT_TEMPLATE = """
你是一个智能财务助手，名为"小财"。你可以帮助用户分析消费情况、提供理财建议、回答财务相关问题。

用户财务概况：
%s

请基于用户的实际财务数据提供建议和分析。回答要求：
1. 使用中文回复
2. 引用具体数据时请准确
3. 如果用户问的问题与财务无关，可以礼貌地引导回财务话题
4. 适当使用 emoji 让回复更生动
5. 金额单位为元（保留两位小数）
"""
    }

    /**
     * 构建包含用户财务数据的 system prompt
     */
    suspend fun buildSystemPrompt(): String {
        val context = buildRagContext()
        return SYSTEM_PROMPT_TEMPLATE.format(context)
    }

    /**
     * 构建 RAG 上下文文本
     */
    private suspend fun buildRagContext(): String {
        val sb = StringBuilder()

        try {
            // 获取本月时间范围
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStart = calendar.timeInMillis

            val now = System.currentTimeMillis()

            // 本月收支
            val monthExpense = billRepository.getTotalExpense(monthStart, now).first()
            val monthIncome = billRepository.getTotalIncome(monthStart, now).first()

            sb.appendLine("- 本月支出：${formatAmount(monthExpense)} 元")
            sb.appendLine("- 本月收入：${formatAmount(monthIncome)} 元")
            sb.appendLine("- 本月净收支：${formatAmount(monthIncome - monthExpense)} 元")

            // 分类支出
            val categoryBreakdown = billRepository.getCategoryBreakdown(monthStart, now).first()
            if (categoryBreakdown.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("本月支出分类：")
                categoryBreakdown.entries
                    .sortedByDescending { it.value }
                    .forEach { (category, amount) ->
                        sb.appendLine("  - ${category.icon} ${category.displayName}：${formatAmount(amount)} 元")
                    }
            }

            // 最近交易
            val recentBills = billRepository.getRecentBills(10).first()
            if (recentBills.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("最近交易记录：")
                val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.CHINA)
                recentBills.forEach { bill ->
                    val type = if (bill.type == BillType.EXPENSE) "支出" else "收入"
                    val date = dateFormat.format(Date(bill.timestamp))
                    val merchant = if (bill.merchant.isNotEmpty()) " (${bill.merchant})" else ""
                    sb.appendLine("  - [$date] $type ${formatAmount(bill.amount)} 元 ${bill.category.displayName}$merchant")
                }
            }
        } catch (e: Exception) {
            sb.appendLine("（暂无财务数据）")
        }

        return sb.toString()
    }

    private fun formatAmount(amountInCents: Long): String {
        return "%.2f".format(amountInCents / 100.0)
    }
}
