package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.Category
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CategoryClassifierTest {

    private lateinit var classifier: CategoryClassifier

    @Before
    fun setup() {
        classifier = CategoryClassifier()
    }

    @Test
    fun `classify food merchants`() {
        val foodMerchants = listOf("美团", "饿了么", "肯德基", "星巴克", "瑞幸咖啡", "海底捞")
        foodMerchants.forEach { merchant ->
            val (category, _) = classifier.classify(merchant)
            assertEquals("Expected FOOD for $merchant", Category.FOOD, category)
        }
    }

    @Test
    fun `classify transport merchants`() {
        val transportMerchants = listOf("滴滴出行", "高德地图", "12306", "携程")
        transportMerchants.forEach { merchant ->
            val (category, _) = classifier.classify(merchant)
            assertEquals("Expected TRANSPORT for $merchant", Category.TRANSPORT, category)
        }
    }

    @Test
    fun `classify shopping merchants`() {
        val shoppingMerchants = listOf("淘宝", "京东", "拼多多", "天猫超市")
        shoppingMerchants.forEach { merchant ->
            val (category, _) = classifier.classify(merchant)
            assertEquals("Expected SHOPPING for $merchant", Category.SHOPPING, category)
        }
    }

    @Test
    fun `classify entertainment merchants`() {
        val merchants = listOf("电影", "游戏充值", "KTV", "健身")
        merchants.forEach { merchant ->
            val (category, _) = classifier.classify(merchant)
            assertEquals("Expected ENTERTAINMENT for $merchant", Category.ENTERTAINMENT, category)
        }
    }

    @Test
    fun `classify housing merchants`() {
        val merchants = listOf("房租", "物业费", "水电费", "燃气费")
        merchants.forEach { merchant ->
            val (category, _) = classifier.classify(merchant)
            assertEquals("Expected HOUSING for $merchant", Category.HOUSING, category)
        }
    }

    @Test
    fun `classify medical merchants`() {
        val merchants = listOf("医院", "药店", "诊所", "体检中心")
        merchants.forEach { merchant ->
            val (category, _) = classifier.classify(merchant)
            assertEquals("Expected MEDICAL for $merchant", Category.MEDICAL, category)
        }
    }

    @Test
    fun `unknown merchant returns OTHER`() {
        val (category, _) = classifier.classify("未知商户")
        assertEquals(Category.OTHER, category)
    }

    @Test
    fun `classify is case insensitive`() {
        val (category, _) = classifier.classify("美团外卖")
        assertEquals(Category.FOOD, category)
    }
}
