package com.eta.assistant.data.repository

import androidx.room.withTransaction
import com.eta.assistant.data.local.EtaDatabase
import com.eta.assistant.data.local.dao.CustomerWithBalance
import com.eta.assistant.data.local.dao.SaleWithItems
import com.eta.assistant.data.local.dao.TopProductSummary
import com.eta.assistant.data.local.entity.CustomerEntity
import com.eta.assistant.data.local.entity.ExpenseEntity
import com.eta.assistant.data.local.entity.PaymentEntity
import com.eta.assistant.data.local.entity.ProductEntity
import com.eta.assistant.data.local.entity.SaleEntity
import com.eta.assistant.data.local.entity.SaleItemEntity
import com.eta.assistant.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

data class CartItem(
    val product: ProductEntity,
    val quantity: Double
) {
    val subtotal: Double get() = product.sellingPrice * quantity
}

sealed class SaleResult {
    data class Success(val saleId: Long) : SaleResult()
    data class Error(val message: String) : SaleResult()
}

class EtaRepository(private val database: EtaDatabase) {
    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val saleDao = database.saleDao()
    private val paymentDao = database.paymentDao()
    private val expenseDao = database.expenseDao()
    private val stockMovementDao = database.stockMovementDao()

    // Products
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()
    fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)
    fun getLowStockCount(): Flow<Int> = productDao.getLowStockCount()

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    suspend fun addProduct(product: ProductEntity): Long {
        val id = productDao.insertProduct(product)
        if (product.stockQuantity > 0) {
            stockMovementDao.insertMovement(
                StockMovementEntity(
                    productId = id,
                    changeQuantity = product.stockQuantity,
                    reason = "INITIAL_STOCK"
                )
            )
        }
        return id
    }

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    suspend fun adjustProductStock(productId: Long, change: Double, reason: String) {
        database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction
            val newQty = product.stockQuantity + change
            if (newQty < 0) throw IllegalStateException("Stock cannot be negative")
            productDao.adjustStock(productId, change)
            stockMovementDao.insertMovement(
                StockMovementEntity(
                    productId = productId,
                    changeQuantity = change,
                    reason = reason
                )
            )
        }
    }

    // Customers
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    fun getCustomersWithBalances(): Flow<List<CustomerWithBalance>> = customerDao.getCustomersWithBalances()
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    fun getCustomerBalance(customerId: Long): Flow<CustomerWithBalance?> = customerDao.getCustomerBalance(customerId)
    fun getTotalOutstandingDues(): Flow<Double> = customerDao.getTotalOutstandingDues()

    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)
    suspend fun addCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    // Sales & Checkout (Atomic Transaction)
    suspend fun recordSale(
        customerId: Long?,
        customerName: String,
        items: List<CartItem>,
        paidAmount: Double,
        paymentMethod: String,
        notes: String = ""
    ): SaleResult {
        if (items.isEmpty()) {
            return SaleResult.Error("Cart cannot be empty")
        }

        return try {
            database.withTransaction {
                // 1. Verify stock availability
                for (item in items) {
                    val currentProduct = productDao.getProductById(item.product.id)
                        ?: return@withTransaction SaleResult.Error("Product ${item.product.name} not found")
                    if (currentProduct.stockQuantity < item.quantity) {
                        return@withTransaction SaleResult.Error(
                            "Insufficient stock for ${currentProduct.name}. Available: ${currentProduct.stockQuantity}, Requested: ${item.quantity}"
                        )
                    }
                }

                // 2. Calculate totals
                val totalAmount = items.sumOf { it.subtotal }
                if (paidAmount < 0 || paidAmount > totalAmount) {
                    return@withTransaction SaleResult.Error("Invalid paid amount")
                }

                // 3. Insert sale
                val sale = SaleEntity(
                    customerId = customerId,
                    customerName = customerName,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    paymentMethod = paymentMethod,
                    notes = notes
                )
                val saleId = saleDao.insertSale(sale)

                // 4. Insert items and decrement stock
                val saleItems = items.map {
                    SaleItemEntity(
                        saleId = saleId,
                        productId = it.product.id,
                        productName = it.product.name,
                        quantity = it.quantity,
                        unitPrice = it.product.sellingPrice,
                        subtotal = it.subtotal
                    )
                }
                saleDao.insertSaleItems(saleItems)

                for (item in items) {
                    productDao.adjustStock(item.product.id, -item.quantity)
                    stockMovementDao.insertMovement(
                        StockMovementEntity(
                            productId = item.product.id,
                            changeQuantity = -item.quantity,
                            reason = "SALE"
                        )
                    )
                }

                // 5. If partial or full payment made for customer, record payment if applicable
                if (customerId != null && paidAmount > 0) {
                    paymentDao.insertPayment(
                        PaymentEntity(
                            customerId = customerId,
                            saleId = saleId,
                            amount = paidAmount,
                            paymentMethod = paymentMethod,
                            notes = "Payment at checkout"
                        )
                    )
                }

                SaleResult.Success(saleId)
            }
        } catch (e: Exception) {
            SaleResult.Error(e.message ?: "Failed to record sale")
        }
    }

    suspend fun getSaleWithItems(saleId: Long): SaleWithItems? = saleDao.getSaleWithItems(saleId)
    fun getAllSales(): Flow<List<SaleEntity>> = saleDao.getAllSales()
    fun getRecentSales(limit: Int = 10): Flow<List<SaleEntity>> = saleDao.getRecentSales(limit)
    fun getSalesForCustomer(customerId: Long): Flow<List<SaleEntity>> = saleDao.getSalesForCustomer(customerId)

    suspend fun cancelSale(saleId: Long) {
        database.withTransaction {
            val saleWithItems = saleDao.getSaleWithItems(saleId) ?: return@withTransaction
            if (saleWithItems.sale.status == "CANCELLED") return@withTransaction

            // Restore product stocks
            for (item in saleWithItems.items) {
                productDao.adjustStock(item.productId, item.quantity)
                stockMovementDao.insertMovement(
                    StockMovementEntity(
                        productId = item.productId,
                        changeQuantity = item.quantity,
                        reason = "SALE_CANCELLED_REFUND"
                    )
                )
            }

            saleDao.cancelSale(saleId)
        }
    }

    // Payments
    suspend fun recordCustomerPayment(
        customerId: Long,
        amount: Double,
        paymentMethod: String,
        notes: String = ""
    ): Long {
        return paymentDao.insertPayment(
            PaymentEntity(
                customerId = customerId,
                amount = amount,
                paymentMethod = paymentMethod,
                notes = notes
            )
        )
    }

    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForCustomer(customerId)

    // Expenses
    suspend fun addExpense(title: String, category: String, amount: Double, notes: String = ""): Long {
        return expenseDao.insertExpense(
            ExpenseEntity(
                title = title,
                category = category,
                amount = amount,
                notes = notes
            )
        )
    }

    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    // Analytics & Metrics
    fun getSalesAmountBetween(start: Long, end: Long): Flow<Double> = saleDao.getTotalSalesAmount(start, end)
    fun getItemsSoldBetween(start: Long, end: Long): Flow<Double> = saleDao.getTotalItemsSold(start, end)
    fun getTransactionCountBetween(start: Long, end: Long): Flow<Int> = saleDao.getTransactionCount(start, end)
    fun getExpensesAmountBetween(start: Long, end: Long): Flow<Double> = expenseDao.getTotalExpensesBetween(start, end)
    fun getTopSellingProducts(start: Long, end: Long, limit: Int = 5): Flow<List<TopProductSummary>> =
        saleDao.getTopSellingProducts(start, end, limit)

    // Demo Data Seeder based on UI Mockup
    suspend fun seedDemoDataIfEmpty() {
        val existingProducts = productDao.getAllProducts().first()
        if (existingProducts.isNotEmpty()) return

        database.withTransaction {
            // Seed Mockup Products
            val p1 = productDao.insertProduct(ProductEntity(name = "Rice (5kg)", category = "Groceries", unit = "bag", costPrice = 380.0, sellingPrice = 500.0, stockQuantity = 10.0, lowStockThreshold = 15.0))
            val p2 = productDao.insertProduct(ProductEntity(name = "Sugar (1kg)", category = "Groceries", unit = "kg", costPrice = 42.0, sellingPrice = 50.0, stockQuantity = 5.0, lowStockThreshold = 10.0))
            val p3 = productDao.insertProduct(ProductEntity(name = "Cooking Oil (1L)", category = "Groceries", unit = "bottle", costPrice = 110.0, sellingPrice = 135.0, stockQuantity = 12.0, lowStockThreshold = 15.0))
            val p4 = productDao.insertProduct(ProductEntity(name = "Biscuits", category = "Snacks", unit = "packet", costPrice = 20.0, sellingPrice = 30.0, stockQuantity = 20.0, lowStockThreshold = 8.0))
            val p5 = productDao.insertProduct(ProductEntity(name = "Tea Powder (250g)", category = "Groceries", unit = "box", costPrice = 85.0, sellingPrice = 110.0, stockQuantity = 8.0, lowStockThreshold = 10.0))

            // Seed Mockup Customers
            val c1 = customerDao.insertCustomer(CustomerEntity(name = "Ravi", phone = "+91 98765 43210", notes = "Regular grocery buyer"))
            val c2 = customerDao.insertCustomer(CustomerEntity(name = "Suresh", phone = "+91 98123 45678", notes = "Monthly credit customer"))
            val c3 = customerDao.insertCustomer(CustomerEntity(name = "Priya", phone = "+91 97111 22334", notes = "UPI payments"))
            val c4 = customerDao.insertCustomer(CustomerEntity(name = "Mani", phone = "+91 96222 33445", notes = "Weekly settlement"))
            val c5 = customerDao.insertCustomer(CustomerEntity(name = "Lakshmi", phone = "+91 95333 44556", notes = "Always clear"))

            // Seed Mockup Sales & Due balances matching the screenshot (Ravi ₹ 500 due, Suresh ₹ 1,200 due, Mani ₹ 350 due)
            val now = System.currentTimeMillis()
            val oneDayAgo = now - 86400000L

            // Sale 1: Ravi bought Rice 5kg ₹500, paid 0 (Credit) -> ₹500 Due
            val s1 = saleDao.insertSale(SaleEntity(customerId = c1, customerName = "Ravi", totalAmount = 500.0, paidAmount = 0.0, paymentMethod = "CREDIT", createdAt = oneDayAgo))
            saleDao.insertSaleItems(listOf(SaleItemEntity(saleId = s1, productId = p1, productName = "Rice (5kg)", quantity = 1.0, unitPrice = 500.0, subtotal = 500.0)))

            // Sale 2: Suresh bought ₹1,200, paid 0 -> ₹1,200 Due
            val s2 = saleDao.insertSale(SaleEntity(customerId = c2, customerName = "Suresh", totalAmount = 1200.0, paidAmount = 0.0, paymentMethod = "CREDIT", createdAt = oneDayAgo))
            saleDao.insertSaleItems(listOf(SaleItemEntity(saleId = s2, productId = p1, productName = "Rice (5kg)", quantity = 2.0, unitPrice = 500.0, subtotal = 1000.0), SaleItemEntity(saleId = s2, productId = p2, productName = "Sugar (1kg)", quantity = 4.0, unitPrice = 50.0, subtotal = 200.0)))

            // Sale 3: Mani bought ₹650, paid ₹300 -> ₹350 Due
            val s3 = saleDao.insertSale(SaleEntity(customerId = c4, customerName = "Mani", totalAmount = 650.0, paidAmount = 300.0, paymentMethod = "CASH", createdAt = now))
            saleDao.insertSaleItems(listOf(SaleItemEntity(saleId = s3, productId = p3, productName = "Cooking Oil (1L)", quantity = 2.0, unitPrice = 135.0, subtotal = 270.0), SaleItemEntity(saleId = s3, productId = p1, productName = "Rice (5kg)", quantity = 1.0, unitPrice = 380.0, subtotal = 380.0)))

            // Sale 4: Walk-in today ₹ 1,900 paid full via UPI
            val s4 = saleDao.insertSale(SaleEntity(customerId = null, customerName = "Walk-in Customer", totalAmount = 1900.0, paidAmount = 1900.0, paymentMethod = "UPI", createdAt = now))
            saleDao.insertSaleItems(listOf(SaleItemEntity(saleId = s4, productId = p1, productName = "Rice (5kg)", quantity = 3.0, unitPrice = 500.0, subtotal = 1500.0), SaleItemEntity(saleId = s4, productId = p4, productName = "Biscuits", quantity = 10.0, unitPrice = 30.0, subtotal = 300.0), SaleItemEntity(saleId = s4, productId = p5, productName = "Tea Powder (250g)", quantity = 1.0, unitPrice = 100.0, subtotal = 100.0)))

            // Seed Mockup Expenses
            expenseDao.insertExpense(ExpenseEntity(title = "Shop Rent", category = "Rent", amount = 1000.0, createdAt = now))
            expenseDao.insertExpense(ExpenseEntity(title = "Electricity", category = "Utilities", amount = 200.0, createdAt = now))
        }
    }

    suspend fun clearAllData() {
        database.withTransaction {
            database.clearAllTables()
        }
    }

    suspend fun resetAndSeedData() {
        clearAllData()
        seedDemoDataIfEmpty()
    }
}

