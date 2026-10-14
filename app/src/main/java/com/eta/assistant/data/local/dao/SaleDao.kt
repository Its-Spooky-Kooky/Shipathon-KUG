package com.eta.assistant.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.eta.assistant.data.local.entity.SaleEntity
import com.eta.assistant.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

data class SaleWithItems(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemEntity>
)

data class TopProductSummary(
    val productId: Long,
    val productName: String,
    val totalQuantitySold: Double,
    val totalRevenue: Double
)

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<SaleEntity>)

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItems(): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSales(limit: Int): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getSalesForCustomer(customerId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getSalesBetween(startTime: Long, endTime: Long): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM sales WHERE createdAt >= :startTime AND createdAt <= :endTime AND status = 'COMPLETED'")
    fun getTotalSalesAmount(startTime: Long, endTime: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(si.quantity), 0.0) 
        FROM sale_items si 
        INNER JOIN sales s ON si.saleId = s.id 
        WHERE s.createdAt >= :startTime AND s.createdAt <= :endTime AND s.status = 'COMPLETED'
    """)
    fun getTotalItemsSold(startTime: Long, endTime: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(si.quantity * p.costPrice), 0.0)
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        INNER JOIN products p ON si.productId = p.id
        WHERE s.status != 'CANCELLED' AND s.createdAt >= :startTime AND s.createdAt <= :endTime
    """)
    fun getCostOfGoodsSold(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :startTime AND createdAt <= :endTime AND status = 'COMPLETED'")
    fun getTransactionCount(startTime: Long, endTime: Long): Flow<Int>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleWithItems(saleId: Long): SaleWithItems?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Long): List<SaleItemEntity>

    @Query("""
        SELECT 
            si.productId,
            si.productName,
            SUM(si.quantity) as totalQuantitySold,
            SUM(si.subtotal) as totalRevenue
        FROM sale_items si
        INNER JOIN sales s ON si.saleId = s.id
        WHERE s.createdAt >= :startTime AND s.createdAt <= :endTime AND s.status = 'COMPLETED'
        GROUP BY si.productId, si.productName
        ORDER BY totalQuantitySold DESC
        LIMIT :limit
    """)
    fun getTopSellingProducts(startTime: Long, endTime: Long, limit: Int = 5): Flow<List<TopProductSummary>>

    @Query("UPDATE sales SET status = 'CANCELLED' WHERE id = :saleId")
    suspend fun cancelSale(saleId: Long)
}

