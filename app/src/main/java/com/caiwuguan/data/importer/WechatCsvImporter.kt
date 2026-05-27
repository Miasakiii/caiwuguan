package com.caiwuguan.data.importer

import com.caiwuguan.data.parser.CategoryClassifier
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Deduplicator
import com.caiwuguan.domain.model.PaymentSource
import com.caiwuguan.domain.repository.BillRepository
import com.caiwuguan.util.AmountExtractor
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 微信账单 CSV 导入器
 */
@Singleton
class WechatCsvImporter @Inject constructor(
    private val billRepository: BillRepository,
    private val categoryClassifier: CategoryClassifier,
    private val deduplicator: Deduplicator
) : Importer {

    companion object {
        // 微信 CSV 格式示例：
        // 交易时间,交易类型,交易对方,商品,收/支,金额(元),支付方式,当前状态,交易单号,商户单号,备注
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    }

    override suspend fun import(inputStream: java.io.InputStream): ImportResult {
        var importedCount = 0
        var duplicateCount = 0
        var failedCount = 0

        try {
            val reader = BufferedReader(InputStreamReader(inputStream, "GBK"))

            // 跳过前16行（微信账单头部信息）
            for (i in 1..16) {
                reader.readLine()
            }

            // 读取表头
            val header = reader.readLine()
            if (header == null) {
                return ImportResult.Error("文件格式错误：缺少表头")
            }

            // 逐行读取数据
            var line = reader.readLine()
            while (line != null) {
                try {
                    val bill = parseLine(line)
                    if (bill != null) {
                        // 检查是否重复
                        val checkResult = deduplicator.check(bill)
                        if (checkResult is com.caiwuguan.domain.model.DuplicateCheckResult.Duplicate) {
                            duplicateCount++
                        } else {
                            billRepository.insertBill(bill)
                            importedCount++
                        }
                    } else {
                        failedCount++
                    }
                } catch (e: Exception) {
                    failedCount++
                }
                line = reader.readLine()
            }

            reader.close()
            return ImportResult.Success(importedCount, duplicateCount, failedCount)
        } catch (e: Exception) {
            return ImportResult.Error("导入失败：${e.message}")
        }
    }

    private fun parseLine(line: String): Bill? {
        // 解析 CSV 行（处理引号内的逗号）
        val fields = parseCsvLine(line)
        if (fields.size < 11) return null

        val dateStr = fields[0].trim()
        val transactionType = fields[1].trim()
        val counterparty = fields[2].trim()
        val product = fields[3].trim()
        val direction = fields[4].trim() // 收/支/"/"
        val amountStr = fields[5].trim()
        val status = fields[7].trim()

        // 跳过未完成的交易
        if (status != "支付成功" && status != "已收钱" && status != "已转账") {
            return null
        }

        // 跳过特殊交易类型
        if (transactionType in listOf("零钱提现", "零钱充值", "理财通", "信用卡还款")) {
            return null
        }

        // 解析金额
        val amount = parseAmount(amountStr) ?: return null

        // 解析日期
        val timestamp = try {
            DATE_FORMAT.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        // 确定交易类型
        val billType = when (direction) {
            "支出" -> BillType.EXPENSE
            "收入" -> BillType.INCOME
            else -> return null // 跳过其他类型
        }

        // 分类
        val merchant = counterparty.ifBlank { product }
        val (category, _) = categoryClassifier.classify(merchant)

        return Bill(
            amount = amount,
            type = billType,
            category = category,
            merchant = merchant,
            description = product,
            source = PaymentSource.WECHAT,
            transactionId = null,
            notificationText = line,
            isAutoRecorded = false,
            timestamp = timestamp
        )
    }

    private fun parseAmount(amountStr: String): Long? {
        // 移除 ¥ 符号和空格
        val cleanAmount = amountStr
            .replace("¥", "")
            .replace("￥", "")
            .replace(" ", "")
            .replace(",", "")

        return try {
            val amount = cleanAmount.toDouble()
            (amount * 100).toLong()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        fields.add(current.toString())

        return fields
    }
}
