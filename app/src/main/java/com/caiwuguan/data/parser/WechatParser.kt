package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import com.caiwuguan.util.AmountExtractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WechatParser @Inject constructor() : NotificationParser {

    override fun canParse(packageName: String): Boolean =
        packageName == "com.tencent.mm"

    override fun parse(text: String, packageName: String): ParseResult {
        if (shouldIgnore(text)) return ParseResult.Ignore

        val amount = AmountExtractor.extractAmount(text) ?: return ParseResult.Failure("无法提取金额")

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
                val merchant = AmountExtractor.extractAfterGive(text)
                ParseResult.Success(
                    amount, BillType.EXPENSE, merchant = merchant ?: "",
                    category = Category.TRANSFER, source = PaymentSource.WECHAT
                )
            }

            text.contains("付款") -> {
                val merchant = AmountExtractor.extractMerchant(text) ?: ""
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
}
