package com.caiwuguan.data.exporter

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.caiwuguan.domain.model.Bill
import com.caiwuguan.domain.model.BillType
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CSV 导出器
 */
@Singleton
class CsvExporter @Inject constructor() {

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        private val FILE_DATE_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
    }

    /**
     * 导出账单为 CSV 文件
     */
    fun exportToCsv(context: Context, bills: List<Bill>): Uri? {
        try {
            val fileName = "财务官_${FILE_DATE_FORMAT.format(Date())}.csv"
            val file = File(context.cacheDir, fileName)

            FileWriter(file).use { writer ->
                // 写入表头
                writer.append("交易时间,交易类型,分类,商户,金额(元),来源,备注\n")

                // 写入数据
                bills.forEach { bill ->
                    val dateStr = DATE_FORMAT.format(Date(bill.timestamp))
                    val typeStr = if (bill.type == BillType.EXPENSE) "支出" else "收入"
                    val amountStr = "%.2f".format(bill.amount / 100.0)
                    val sourceStr = getSourceName(bill.source.name)
                    val description = bill.description.replace(",", "，").replace("\n", " ")

                    writer.append("$dateStr,$typeStr,${bill.category.displayName},${bill.merchant},$amountStr,$sourceStr,$description\n")
                }
            }

            // 获取文件 Uri
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 创建分享 Intent
     */
    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun getSourceName(source: String): String {
        return when {
            source.startsWith("WECHAT") -> "微信"
            source.startsWith("ALIPAY") -> "支付宝"
            source.startsWith("BANK_") -> "银行"
            source == "MANUAL" -> "手动"
            else -> source
        }
    }
}
