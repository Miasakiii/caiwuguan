package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WechatParserTest {

    private lateinit var parser: WechatParser

    @Before
    fun setup() {
        parser = WechatParser()
    }

    @Test
    fun `canParse returns true for WeChat package`() {
        assertTrue(parser.canParse("com.tencent.mm"))
    }

    @Test
    fun `canParse returns false for other packages`() {
        assertFalse(parser.canParse("com.eg.android.AlipayGphone"))
        assertFalse(parser.parse("test", "com.other.app") is ParseResult.Success)
    }

    @Test
    fun `parse payment notification extracts amount and merchant`() {
        val text = "你已成功向「星巴克」付款 ¥32.00"
        val result = parser.parse(text, "com.tencent.mm")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(3200L, success.amount)
        assertEquals(BillType.EXPENSE, success.type)
        assertEquals("星巴克", success.merchant)
        assertEquals(PaymentSource.WECHAT, success.source)
    }

    @Test
    fun `parse receipt notification returns income`() {
        val text = "微信支付收款到账 ¥25.00"
        val result = parser.parse(text, "com.tencent.mm")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(2500L, success.amount)
        assertEquals(BillType.INCOME, success.type)
    }

    @Test
    fun `parse red packet notification`() {
        val text = "你收到了一个红包 ¥8.88"
        val result = parser.parse(text, "com.tencent.mm")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(888L, success.amount)
        assertEquals(BillType.INCOME, success.type)
        assertEquals(Category.RED_PACKET, success.category)
    }

    @Test
    fun `parse transfer notification`() {
        val text = "你已转账 ¥100.00 给 张三"
        val result = parser.parse(text, "com.tencent.mm")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(10000L, success.amount)
        assertEquals(BillType.EXPENSE, success.type)
        assertEquals(Category.TRANSFER, success.category)
    }

    @Test
    fun `parse refund notification returns income`() {
        val text = "微信支付退款 ¥10.00"
        val result = parser.parse(text, "com.tencent.mm")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1000L, success.amount)
        assertEquals(BillType.INCOME, success.type)
    }

    @Test
    fun `parse ignored notifications returns ignore`() {
        val texts = listOf(
            "零钱提现 ¥100.00",
            "零钱通收益到账 ¥0.50",
            "理财通收益到账 ¥1.20"
        )
        texts.forEach { text ->
            val result = parser.parse(text, "com.tencent.mm")
            assertTrue("Expected Ignore for: $text", result is ParseResult.Ignore)
        }
    }

    @Test
    fun `parse unrecognized notification returns failure`() {
        val text = "这是一条普通通知"
        val result = parser.parse(text, "com.tencent.mm")

        assertTrue(result is ParseResult.Failure)
    }

    @Test
    fun `parse amount with comma separator`() {
        val text = "你已成功向「超市」付款 ¥1,234.56"
        val result = parser.parse(text, "com.tencent.mm")

        assertTrue(result is ParseResult.Success)
        assertEquals(123456L, (result as ParseResult.Success).amount)
    }
}
