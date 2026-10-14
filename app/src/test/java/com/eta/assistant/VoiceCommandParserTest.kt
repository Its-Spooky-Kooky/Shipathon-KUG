package com.eta.assistant

import com.eta.assistant.ui.screens.voice.ParsedIntent
import com.eta.assistant.ui.screens.voice.VoiceCommandParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {

    @Test
    fun testRecordSaleSimple() {
        val result = VoiceCommandParser.parse("Record a ₹500 sale for Ravi")
        assertTrue(result is ParsedIntent.RecordSale)
        val sale = result as ParsedIntent.RecordSale
        assertEquals("Ravi", sale.customerName)
        assertEquals(500.0, sale.amount ?: 0.0, 0.001)
    }

    @Test
    fun testRecordSaleWithQuantity() {
        val result = VoiceCommandParser.parse("sale of 2 notebooks for Ravi")
        assertTrue(result is ParsedIntent.RecordSale)
        val sale = result as ParsedIntent.RecordSale
        assertEquals("Ravi", sale.customerName)
        assertEquals(2.0, sale.quantity, 0.001)
        assertEquals("notebooks", sale.productName)
    }

    @Test
    fun testAddStockCommand() {
        val result = VoiceCommandParser.parse("Add 5kg rice to stock")
        assertTrue(result is ParsedIntent.AddStock)
        val stock = result as ParsedIntent.AddStock
        assertEquals("rice", stock.productName)
        assertEquals(5.0, stock.quantity, 0.001)
    }

    @Test
    fun testShowDuesCommand() {
        val result = VoiceCommandParser.parse("Show my dues")
        assertTrue(result is ParsedIntent.ShowDues)
    }

    @Test
    fun testTopSellingCommand() {
        val result = VoiceCommandParser.parse("What are my top selling items?")
        assertTrue(result is ParsedIntent.ShowTopSelling)
    }

    @Test
    fun testHindiShowDues() {
        val result = VoiceCommandParser.parse("बकाया दिखाओ")
        assertTrue(result is ParsedIntent.ShowDues)
    }

    @Test
    fun testTamilShowDues() {
        val result = VoiceCommandParser.parse("நிலுவை காட்டு")
        assertTrue(result is ParsedIntent.ShowDues)
    }

    @Test
    fun testBengaliShowDues() {
        val result = VoiceCommandParser.parse("বকেয়া দেখাও")
        assertTrue(result is ParsedIntent.ShowDues)
    }

    @Test
    fun testHindiTopSelling() {
        val result = VoiceCommandParser.parse("सबसे ज्यादा बिकने वाला")
        assertTrue(result is ParsedIntent.ShowTopSelling)
    }

    @Test
    fun testHindiSovCommand() {
        val result = VoiceCommandParser.parse("500 rupay ki chini Ramesh ke khate mein likh do")
        assertTrue(result is ParsedIntent.RecordSale)
        val sale = result as ParsedIntent.RecordSale
        assertEquals("Ramesh", sale.customerName)
        assertEquals("chini", sale.productName)
        assertEquals(500.0, sale.amount ?: 0.0, 0.001)
        assertTrue(sale.isCredit)
    }

    @Test
    fun testCustomerFirstCreditCommand() {
        val result = VoiceCommandParser.parse("Ramesh 2 kg rice udhaar")
        assertTrue(result is ParsedIntent.RecordSale)
        val sale = result as ParsedIntent.RecordSale
        assertEquals("Ramesh", sale.customerName)
        assertEquals("rice", sale.productName)
        assertEquals(2.0, sale.quantity, 0.001)
        assertTrue(sale.isCredit)
    }

    @Test
    fun testCustomerFirstAmountCommand() {
        val result = VoiceCommandParser.parse("Ramesh 2 kg chini 100 rupay")
        assertTrue(result is ParsedIntent.RecordSale)
        val sale = result as ParsedIntent.RecordSale
        assertEquals("Ramesh", sale.customerName)
        assertEquals("chini", sale.productName)
        assertEquals(2.0, sale.quantity, 0.001)
        assertEquals(100.0, sale.amount ?: 0.0, 0.001)
        org.junit.Assert.assertFalse(sale.isCredit)
    }

    @Test
    fun testSimpleAmountCustomerCommand() {
        val result = VoiceCommandParser.parse("₹500 Ramesh")
        assertTrue(result is ParsedIntent.RecordSale)
        val sale = result as ParsedIntent.RecordSale
        assertEquals("Ramesh", sale.customerName)
        assertEquals(500.0, sale.amount ?: 0.0, 0.001)
    }
}
