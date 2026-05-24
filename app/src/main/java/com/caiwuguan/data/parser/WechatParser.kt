package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WechatParser @Inject constructor() : NotificationParser {

    override fun canParse(packageName: String): Boolean =
        packageName == "com.tencent.mm"

    override fun parse(text: String, packageName: String): ParseResult {
        if (shouldIgnore(text)) return ParseResult.Ignore

        val amount = extractAmount(text) ?: return ParseResult.Failure("无法提取金额")

        return when {
            text.contains("退款") -> ParseResult.Success(
                amount, BillType.INCOME, source = PaymentSource.WECHAT
            )

            text.contains("收款到账") || text.contains("到账") -> ParseResult.Success(
                amount, BillType.INCOME, source = PaymentSource.WECHAT
            )

            text.contains("红包") -> ParseResult.Success(
                amount, BillType.INCOME, merchant = "红包",
                category = Category.RED_PACKET, source = PaymentSource.WECHAT
            )

            text.contains("转账") -> {
                val merchant = extractAfterGive(text)
                ParseResult.Success(
                    amount, BillType.EXPENSE, merchant = merchant ?: "",
                    category = Category.TRANSFER, source = PaymentSource.WECHAT
                )
            }

            text.contains("付款") -> {
                val merchant = extractQuoteContent(text)
                    ?: extractAfterGive(text)
                    ?: ""
                ParseResult.Success(
                    amount, BillType.EXPENSE, merchant = merchant,
                    source = PaymentSource.WECHAT
                )
            }

            text.contains("支付") -> ParseResult.Success(
                amount, BillType.EXPENSE, source = PaymentSource.WECHAT
            )

            else -> ParseResult.Failure("无法识别通知类型")
        }
    }

    private fun shouldIgnore(text: String): Boolean =
        text.contains("零钱提现") || text.contains("零钱通") || text.contains("理财通")

    private fun extractAmount(text: String): Long? {
        val regex = Regex("""[￥¥]\s*([0-9,]+\.\d{2})""")
        val match = regex.find(text) ?: return null
        val amountStr = match.groupValues[1].replace(",", "")
        return BigDecimal(amountStr).multiply(BigDecimal(100)).toLong()
    }

    private fun extractQuoteContent(text: String): String? {
        val regex = Regex("""「(.+?)」""")
        return regex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun extractAfterGive(text: String): String? {
        val regex = Regex("""给\s*(.+)$""")
        return regex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }
    }
}
