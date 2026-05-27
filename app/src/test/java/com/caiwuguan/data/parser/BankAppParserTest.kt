package com.caiwuguan.data.parser

import com.caiwuguan.domain.model.BillType
import com.caiwuguan.domain.model.Category
import com.caiwuguan.domain.model.PaymentSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BankAppParserTest {

    private lateinit var parser: BankAppParser

    @Before
    fun setup() {
        parser = BankAppParser()
    }

    @Test
    fun `canParse returns true for supported bank packages`() {
        assertTrue(parser.canParse("com.icbc"))
        assertTrue(parser.canParse("com.chinamworld.main"))
        assertTrue(parser.canParse("com.android.bankabc"))
        assertTrue(parser.canParse("com.boc"))
        assertTrue(parser.canParse("com.bankcomm"))
        assertTrue(parser.canParse("cmb.pb"))
    }

    @Test
    fun `canParse returns false for unsupported packages`() {
        assertFalse(parser.canParse("com.tencent.mm"))
        assertFalse(parser.canParse("com.unknown.bank"))
    }

    @Test
    fun `parse ICBC consumption notification`() {
        val text = "【工商银行】您尾号1234的卡片消费人民币158.00元"
        val result = parser.parse(text, "com.icbc")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(15800L, success.amount)
        assertEquals(BillType.EXPENSE, success.type)
        assertEquals(PaymentSource.BANK_ICBC, success.source)
    }

    @Test
    fun `parse CCB salary notification`() {
        val text = "【建设银行】...工资入账15000.00元"
        val result = parser.parse(text, "com.chinamworld.main")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1500000L, success.amount)
        assertEquals(BillType.INCOME, success.type)
        assertEquals(Category.SALARY, success.category)
    }

    @Test
    fun `parse refund notification`() {
        val text = "【招商银行】退款 ¥50.00"
        val result = parser.parse(text, "cmb.pb")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(5000L, success.amount)
        assertEquals(BillType.INCOME, success.type)
    }

    @Test
    fun `parse transfer notification`() {
        val text = "【中国银行】转账支出 ¥1000.00"
        val result = parser.parse(text, "com.boc")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(100000L, success.amount)
        assertEquals(BillType.EXPENSE, success.type)
        assertEquals(Category.TRANSFER, success.category)
    }

    @Test
    fun `parse withdrawal notification`() {
        val text = "【交通银行】取款 ¥500.00"
        val result = parser.parse(text, "com.bankcomm")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(50000L, success.amount)
        assertEquals(BillType.EXPENSE, success.type)
    }

    @Test
    fun `parse deposit notification`() {
        val text = "【农业银行】存入 ¥2000.00"
        val result = parser.parse(text, "com.android.bankabc")

        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(200000L, success.amount)
        assertEquals(BillType.INCOME, success.type)
    }

    @Test
    fun `parse amount with yuan suffix`() {
        val text = "消费人民币¥1,234.56元"
        val result = parser.parse(text, "com.icbc")

        assertTrue(result is ParseResult.Success)
        assertEquals(123456L, (result as ParseResult.Success).amount)
    }

    @Test
    fun `unrecognized notification returns failure`() {
        val text = "您有一条新消息"
        val result = parser.parse(text, "com.icbc")

        assertTrue(result is ParseResult.Failure)
    }
}
