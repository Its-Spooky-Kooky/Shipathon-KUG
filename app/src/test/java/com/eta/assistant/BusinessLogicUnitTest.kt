package com.eta.assistant

import com.eta.assistant.core.util.CurrencyFormatter
import com.eta.assistant.data.local.entity.ProductEntity
import com.eta.assistant.data.repository.CartItem
import org.junit.Assert.assertEquals
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
}

