package com.caiwuguan.util

import org.junit.Assert.*
import org.junit.Test

class AmountExtractorTest {

    @Test
    fun `extract amount with yuan sign`() {
        val result = AmountExtractor.extractAmount("付款 ¥25.00")
        assertEquals(2500L, result)
    }

    @Test
    fun `extract amount with fullwidth yuan sign`() {
        val result = AmountExtractor.extractAmount("付款 ￥15.50")
        assertEquals(1550L, result)
    }

    @Test
    fun `extract amount with yuan suffix`() {
        val result = AmountExtractor.extractAmount("消费158.00元")
        assertEquals(15800L, result)
    }

    @Test
    fun `extract amount with comma separator`() {
        val result = AmountExtractor.extractAmount("付款 ¥1,234.56")
        assertEquals(123456L, result)
    }

    @Test
    fun `extract amount with renminbi prefix`() {
        val result = AmountExtractor.extractAmount("消费人民币158.00元")
        assertEquals(15800L, result)
    }

    @Test
    fun `extract amount returns null for no match`() {
        val result = AmountExtractor.extractAmount("没有金额的文本")
        assertNull(result)
    }

    @Test
    fun `extract merchant from quotes`() {
        val result = AmountExtractor.extractMerchant("你已成功向「星巴克」付款 ¥32.00")
        assertEquals("星巴克", result)
    }

    @Test
    fun `extract merchant after give`() {
        val result = AmountExtractor.extractMerchant("你已转账 ¥100.00 给 张三")
        assertEquals("张三", result)
    }

    @Test
    fun `extract merchant returns null when no match`() {
        val result = AmountExtractor.extractMerchant("付款 ¥25.00")
        assertNull(result)
    }

    @Test
    fun `extractAfterGive works correctly`() {
        assertEquals("张三", AmountExtractor.extractAfterGive("转账给 张三"))
        assertNull(AmountExtractor.extractAfterGive("付款 ¥25.00"))
    }
}
