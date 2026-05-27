package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AlipayParserTest {

    private lateinit var parser: AlipayParser

    @Before
    fun setup() {
        parser = AlipayParser()
    }

    @Test
    fun `canParse returns true for Alipay package`() {
        assertTrue(parser.canParse("com.eg.android.AlipayGphone"))
    }

    @Test
    fun `canParse returns false for other packages`() {
        assertFalse(parser.canParse("com.tencent.mm"))
    }

    @Test
    fun `parse payment notification`() {
        val text = "支付宝成功付款 ¥18.00"
        val result = parser.parse(text, "com.eg.android.AlipayGphone")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1800L, success.amount)
        assertEquals(BillType.EXPENSE, success.type)
        assertEquals(PaymentSource.ALIPAY, success.source)
    }

    @Test
    fun `parse payment with merchant`() {
        val text = "你已成功付款 ¥100.00 给 饿了么"
        val result = parser.parse(text, "com.eg.android.AlipayGphone")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(10000L, success.amount)
        assertEquals(BillType.EXPENSE, success.type)
        assertEquals("饿了么", success.merchant)
    }

    @Test
    fun `parse transfer received notification`() {
        val text = "你收到一笔转账 ¥500.00"
        val result = parser.parse(text, "com.eg.android.AlipayGphone")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(50000L, success.amount)
        assertEquals(BillType.INCOME, success.type)
        assertEquals(Category.TRANSFER, success.category)
    }

    @Test
    fun `parse transfer sent notification`() {
        val text = "转账 ¥200.00"
        val result = parser.parse(text, "com.eg.android.AlipayGphone")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(20000L, success.amount)
        assertEquals(BillType.EXPENSE, success.type)
        assertEquals(Category.TRANSFER, success.category)
    }

    @Test
    fun `parse refund notification`() {
        val text = "退款 ¥50.00"
        val result = parser.parse(text, "com.eg.android.AlipayGphone")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(5000L, success.amount)
        assertEquals(BillType.INCOME, success.type)
    }

    @Test
    fun `parse ignored notifications returns ignore`() {
        val texts = listOf(
            "余额宝收益到账 ¥0.50",
            "余利宝收益到账 ¥1.20"
        )
        texts.forEach { text ->
            val result = parser.parse(text, "com.eg.android.AlipayGphone")
            assertTrue("Expected Ignore for: $text", result is ParseResult.Ignore)
        }
    }

    @Test
    fun `parse huabei repayment as transfer`() {
        val text = "花呗自动还款 ¥3,500.00"
        val result = parser.parse(text, "com.eg.android.AlipayGphone")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(350000L, success.amount)
        assertEquals(BillType.EXPENSE, success.type)
        assertEquals(Category.TRANSFER, success.category)
    }

    @Test
    fun `single character pay no longer matches`() {
        val text = "付款成功"
        val result = parser.parse(text, "com.eg.android.AlipayGphone")

        // Should fail because no amount found, not because of single "付"
        assertTrue(result is ParseResult.Failure)
    }
}
