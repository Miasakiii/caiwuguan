package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankAppParser @Inject constructor() : NotificationParser {

    private val bankPackages = mapOf(
        "com.icbc" to PaymentSource.BANK_ICBC,
        "com.chinamworld.main" to PaymentSource.BANK_CCB,
        "com.example.bankabc" to PaymentSource.BANK_ABC,
        "com.boc" to PaymentSource.BANK_BOC,
        "com.bankcomm" to PaymentSource.BANK_COMM,
        "cmb.pb" to PaymentSource.BANK_CMB
    )

    override fun canParse(packageName: String): Boolean =
        bankPackages.keys.any { packageName.startsWith(it) }

    override fun parse(text: String, packageName: String): ParseResult {
        val source = bankPackages.entries.firstOrNull { packageName.startsWith(it.key) }
            ?.value ?: return ParseResult.Ignore

        val amount = extractAmount(text) ?: return ParseResult.Failure("无法提取金额")

        return when {
            text.contains("工资") || text.contains("入账") && !text.contains("消费") -> ParseResult.Success(
                amount, BillType.INCOME, category = Category.SALARY, source = source
            )

            text.contains("消费") || text.contains("支出") || text.contains("付款") || text.contains("扣款") -> ParseResult.Success(
                amount, BillType.EXPENSE, source = source
            )

            text.contains("退款") -> ParseResult.Success(
                amount, BillType.INCOME, source = source
            )

            text.contains("转账") -> {
                val type = if (text.contains("收到") || text.contains("转入")) BillType.INCOME else BillType.EXPENSE
                ParseResult.Success(
                    amount, type, category = Category.TRANSFER, source = source
                )
            }

            text.contains("收入") || text.contains("存入") -> ParseResult.Success(
                amount, BillType.INCOME, source = source
            )

            text.contains("取款") || text.contains("支取") -> ParseResult.Success(
                amount, BillType.EXPENSE, source = source
            )

            else -> ParseResult.Failure("无法识别银行通知类型")
        }
    }

    private fun extractAmount(text: String): Long? {
        val regex = Regex("""(?:人民币)?[￥¥]?\s*([0-9,]+\.\d{2})\s*(?:元)?""")
        val match = regex.find(text) ?: return null
        val amountStr = match.groupValues[1].replace(",", "")
        return BigDecimal(amountStr).multiply(BigDecimal(100)).toLong()
    }
}
