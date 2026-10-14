package com.eta.assistant

import com.eta.assistant.core.util.CurrencyFormatter
import com.eta.assistant.core.util.UpiHelper
import com.eta.assistant.data.local.entity.CustomerEntity
import com.eta.assistant.data.local.entity.ExpenseEntity
import com.eta.assistant.data.local.entity.PaymentEntity
import com.eta.assistant.data.local.entity.ProductEntity
import com.eta.assistant.data.local.entity.SaleEntity
import com.eta.assistant.data.local.entity.SaleItemEntity
import com.eta.assistant.data.repository.CartItem
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessLogicUnitTest {

    @Test
    fun testCartItemSubtotalCalculation() {
        val product = ProductEntity(
            id = 1L,
            name = "Rice (5kg)",
            sellingPrice = 500.0,
            stockQuantity = 10.0
        )
        val cartItem = CartItem(product = product, quantity = 3.0)
        assertEquals(1500.0, cartItem.subtotal, 0.001)
    }

    @Test
    fun testCurrencyFormatting() {
        val formatted = CurrencyFormatter.format(4250.0, "₹")
        assertEquals("₹ 4,250", formatted)
    }

    @Test
    fun testPaiseCurrencyFormatting() {
        val formatted = CurrencyFormatter.format(50000L, "₹")
        assertEquals("₹ 500", formatted)
    }

    @Test
    fun testFloatingPointDriftPrevention() {
        // Test that 0.1 + 0.2 precision is handled cleanly with 2 decimal rounding
        val p1 = ProductEntity(name = "Item 1", sellingPrice = 19.99, stockQuantity = 100.0)
        val p2 = ProductEntity(name = "Item 2", sellingPrice = 29.99, stockQuantity = 100.0)
        val item1 = CartItem(p1, 3.0) // 59.97
        val item2 = CartItem(p2, 7.0) // 209.93
        val total = kotlin.math.round((item1.subtotal + item2.subtotal) * 100.0) / 100.0
        assertEquals(269.90, total, 0.001)
    }

    @Test
    fun testUpiDeepLinkGeneration() {
        val uri = UpiHelper.buildUpiUri(
            upiId = "ravistore@upi",
            businessName = "Ravi Store",
            amount = 650.0,
            note = "Store Bill"
        )
        assertTrue(uri.startsWith("upi://pay?pa=ravistore@upi"))
        assertTrue(uri.contains("am=650.00"))
        assertTrue(uri.contains("cu=INR"))
    }

    @Test
    fun testLedgerPartialPaymentCalculation() {
        // Customer bought for 650, paid 300 at checkout
        val saleTotal = 650.0
        val checkoutPaid = 300.0
        val initialDue = saleTotal - checkoutPaid
        assertEquals(350.0, initialDue, 0.001)

        // Customer pays 200 later via Khata settlement (saleId is NULL)
        val settlementPayment = 200.0
        val remainingDue = initialDue - settlementPayment
        assertEquals(150.0, remainingDue, 0.001)
    }

    @Test
    fun testJsonBackupSerializationAndDeserialization() {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val products = JSONArray().apply {
            put(JSONObject().apply {
                put("id", 1L)
                put("name", "Sugar (1kg)")
                put("sellingPrice", 50.0)
                put("costPrice", 42.0)
                put("stockQuantity", 25.0)
            })
        }
        root.put("products", products)

        val sales = JSONArray().apply {
            put(JSONObject().apply {
                put("id", 101L)
                put("customerName", "Ramesh")
                put("totalAmount", 500.0)
                put("paidAmount", 300.0)
                put("paymentMethod", "CASH")
            })
        }
        root.put("sales", sales)

        val jsonString = root.toString(2)
        val parsedRoot = JSONObject(jsonString)

        assertEquals(1, parsedRoot.getInt("version"))
        val parsedProducts = parsedRoot.getJSONArray("products")
        assertEquals(1, parsedProducts.length())
        assertEquals("Sugar (1kg)", parsedProducts.getJSONObject(0).getString("name"))
        assertEquals(50.0, parsedProducts.getJSONObject(0).getDouble("sellingPrice"), 0.001)

        val parsedSales = parsedRoot.getJSONArray("sales")
        assertEquals(1, parsedSales.length())
        assertEquals("Ramesh", parsedSales.getJSONObject(0).getString("customerName"))
        assertEquals(500.0, parsedSales.getJSONObject(0).getDouble("totalAmount"), 0.001)
        assertEquals(300.0, parsedSales.getJSONObject(0).getDouble("paidAmount"), 0.001)
    }
}
