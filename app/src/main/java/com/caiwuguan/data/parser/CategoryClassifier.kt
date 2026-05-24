package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryClassifier @Inject constructor() {

    private val keywordMap: Map<Category, List<String>> = mapOf(
        Category.FOOD to listOf(
            "美团", "饿了么", "肯德基", "麦当劳", "星巴克", "瑞幸",
            "海底捞", "外卖", "餐厅", "食堂", "餐饮", "咖啡"
        ),
        Category.TRANSPORT to listOf(
            "滴滴", "高德", "地铁", "公交", "出租", "加油",
            "停车", "高速", "铁路", "航空"
        ),
        Category.SHOPPING to listOf(
            "淘宝", "京东", "拼多多", "天猫", "超市", "便利店", "商场"
        ),
        Category.ENTERTAINMENT to listOf(
            "电影", "游戏", "KTV", "健身", "旅游", "酒店"
        ),
        Category.HOUSING to listOf(
            "房租", "物业", "水电", "燃气", "宽带"
        ),
        Category.MEDICAL to listOf(
            "医院", "药店", "诊所", "体检"
        ),
        Category.EDUCATION to listOf(
            "课程", "培训", "教育", "学习"
        )
    )

    private val alipayCategoryMap = mapOf(
        "餐饮美食" to Category.FOOD,
        "美食" to Category.FOOD,
        "交通出行" to Category.TRANSPORT,
        "出行交通" to Category.TRANSPORT,
        "购物" to Category.SHOPPING,
        "日用百货" to Category.SHOPPING,
        "娱乐" to Category.ENTERTAINMENT,
        "休闲娱乐" to Category.ENTERTAINMENT,
        "住房" to Category.HOUSING,
        "房产" to Category.HOUSING,
        "医疗" to Category.MEDICAL,
        "健康" to Category.MEDICAL,
        "教育" to Category.EDUCATION,
        "培训" to Category.EDUCATION,
        "转账" to Category.TRANSFER,
        "红包" to Category.RED_PACKET,
        "工资" to Category.SALARY,
        "投资" to Category.INVESTMENT,
        "理财" to Category.INVESTMENT
    )

    fun classify(
        merchant: String,
        source: PaymentSource? = null,
        alipayCategory: String? = null
    ): Pair<Category, Float> {
        if (alipayCategory != null) {
            val mapped = alipayCategoryMap[alipayCategory]
            if (mapped != null) return mapped to 1.0f
        }

        if (merchant.isBlank()) return Category.OTHER to 0.0f

        for ((category, keywords) in keywordMap) {
            if (keywords.any { merchant.contains(it) }) {
                return category to 0.7f
            }
        }

        return Category.OTHER to 0.0f
    }
}
