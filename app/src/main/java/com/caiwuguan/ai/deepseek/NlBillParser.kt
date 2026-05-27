package com.caiwuguan.ai.deepseek

import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class NlBillResult(
    val amount: Double,
    val type: String, // "expense" or "income"
    val category: String,
    val merchant: String,
    val description: String = ""
)

@Singleton
class NlBillParser @Inject constructor(
    private val deepSeekClient: DeepSeekClient
) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val SYSTEM_PROMPT = """
            你是一个账单解析助手。用户会用自然语言描述一笔收支，你需要解析出结构化的账单信息。

            请以 JSON 格式返回，包含以下字段：
            - amount: 金额（数字，单位：元）
            - type: 类型（"expense" 表示支出，"income" 表示收入）
            - category: 分类（从以下选择：FOOD, TRANSPORT, SHOPPING, ENTERTAINMENT, HOUSING, MEDICAL, EDUCATION, TRANSFER, RED_PACKET, SALARY, INVESTMENT, OTHER）
            - merchant: 商户或对方名称
            - description: 备注描述

            示例：
            用户：午饭花了35块钱
            返回：{"amount": 35.0, "type": "expense", "category": "FOOD", "merchant": "", "description": "午饭"}

            用户：收到工资8000元
            返回：{"amount": 8000.0, "type": "income", "category": "SALARY", "merchant": "", "description": "工资"}

            只返回 JSON，不要有其他文字。
        """.trimIndent()
    }

    suspend fun parse(text: String): Result<NlBillResult> {
        val messages = listOf(
            ChatMessage(role = "system", content = SYSTEM_PROMPT),
            ChatMessage(role = "user", content = text)
        )

        return deepSeekClient.chat(messages).mapCatching { content ->
            // 提取 JSON 部分
            val jsonStr = content.trim().let {
                if (it.startsWith("```")) {
                    it.lines().drop(1).dropLast(1).joinToString("\n")
                } else {
                    it
                }
            }

            json.decodeFromString(NlBillResult.serializer(), jsonStr)
        }
    }

    fun toBillType(type: String): BillType {
        return when (type.lowercase()) {
            "expense", "支出" -> BillType.EXPENSE
            "income", "收入" -> BillType.INCOME
            else -> BillType.EXPENSE
        }
    }

    fun toCategory(category: String): Category {
        return try {
            Category.valueOf(category.uppercase())
        } catch (e: Exception) {
            Category.OTHER
        }
    }

    fun toAmount(yuan: Double): Long {
        return (yuan * 100).toLong()
    }
}
