package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlipayParser @Inject constructor() : NotificationParser {

    override fun canParse(packageName: String): Boolean =
        packageName == "com.eg.android.AlipayGphone"

    override fun parse(text: String, packageName: String): ParseResult {
        if (shouldIgnore(text)) return ParseResult.Ignore

        val amount = extractAmount(text) ?: return ParseResult.Failure("无法提取金额")

        return when {
            text.contains("退款") -> ParseResult.Success(
                amount, BillType.INCOME, source = PaymentSource.ALIPAY
            )

            text.contains("收到") && text.contains("转账") -> ParseResult.Success(
                amount, BillType.INCOME, category = Category.TRANSFER,
                source = PaymentSource.ALIPAY
            )

            text.contains("转账") -> ParseResult.Success(
                amount, BillType.EXPENSE, category = Category.TRANSFER,
                source = PaymentSource.ALIPAY
            )

            text.contains("还款") -> ParseResult.Success(
                amount, BillType.EXPENSE, category = Category.TRANSFER,
                source = PaymentSource.ALIPAY
            )

            text.contains("付款") || text.contains("支付") || text.contains("付") -> {
                val merchant = extractAfterGive(text) ?: ""
                ParseResult.Success(
                    amount, BillType.EXPENSE, merchant = merchant,
                    source = PaymentSource.ALIPAY
                )
            }

            else -> ParseResult.Failure("无法识别通知类型")
        }
    }

    private fun shouldIgnore(text: String): Boolean =
        text.contains("余额宝") || text.contains("余利宝")

    private fun extractAmount(text: String): Long? {
        val regex = Regex("""[￥¥]\s*([0-9,]+\.\d{2})""")
        val match = regex.find(text) ?: return null
        val amountStr = match.groupValues[1].replace(",", "")
        return BigDecimal(amountStr).multiply(BigDecimal(100)).toLong()
    }

    private fun extractAfterGive(text: String): String? {
        val regex = Regex("""给\s*(.+)$""")
        return regex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }
    }
}
