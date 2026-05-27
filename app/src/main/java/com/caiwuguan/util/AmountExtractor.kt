package com.caiwuguan.util

import java.math.BigDecimal

/**
 * 金额提取工具类
 * 统一处理通知文本中的金额解析
 */
object AmountExtractor {

    /**
     * 从文本中提取金额（元），转换为分
     * 支持格式：¥25.00、￥15.50、100.00元、人民币158.00元
     */
    fun extractAmount(text: String): Long? {
        val regex = Regex("""(?:人民币)?[￥¥]?\s*([0-9,]+\.\d{2})\s*(?:元)?""")
        val match = regex.find(text) ?: return null
        val amountStr = match.groupValues[1].replace(",", "")
        return try {
            BigDecimal(amountStr).multiply(BigDecimal(100)).toLong()
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * 从文本中提取商户名
     * 支持格式：「商户名」、给商户名
     */
    fun extractMerchant(text: String): String? {
        return extractQuoteContent(text) ?: extractAfterGive(text)
    }

    private fun extractQuoteContent(text: String): String? {
        val regex = Regex("""「(.+?)」""")
        return regex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun extractAfterGive(text: String): String? {
        val regex = Regex("""给\s*(.+)$""")
        return regex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }
    }
}
