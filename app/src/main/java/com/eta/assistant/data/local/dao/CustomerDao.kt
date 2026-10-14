package com.eta.assistant.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.eta.assistant.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

data class CustomerWithBalance(
    val id: Long,
    val name: String,
    val phone: String,
    val notes: String,
    val createdAt: Long,
    val totalPurchased: Double,
    val totalPaid: Double,
    val totalDue: Double
)

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("""
        SELECT 
            c.id, c.name, c.phone, c.notes, c.createdAt,
            COALESCE(SUM(s.totalAmount), 0.0) AS totalPurchased,
            COALESCE(SUM(s.paidAmount), 0.0) + COALESCE((SELECT SUM(amount) FROM payments WHERE customerId = c.id AND saleId IS NULL), 0.0) AS totalPaid,
            COALESCE(SUM(s.totalAmount), 0.0) - (COALESCE(SUM(s.paidAmount), 0.0) + COALESCE((SELECT SUM(amount) FROM payments WHERE customerId = c.id AND saleId IS NULL), 0.0)) AS totalDue
        FROM customers c
        LEFT JOIN sales s ON s.customerId = c.id AND s.status = 'COMPLETED'
        GROUP BY c.id
        ORDER BY totalDue DESC, c.name ASC
    """)
    fun getCustomersWithBalances(): Flow<List<CustomerWithBalance>>

    @Query("""
        SELECT 
            c.id, c.name, c.phone, c.notes, c.createdAt,
            COALESCE(SUM(s.totalAmount), 0.0) AS totalPurchased,
            COALESCE(SUM(s.paidAmount), 0.0) + COALESCE((SELECT SUM(amount) FROM payments WHERE customerId = c.id AND saleId IS NULL), 0.0) AS totalPaid,
            COALESCE(SUM(s.totalAmount), 0.0) - (COALESCE(SUM(s.paidAmount), 0.0) + COALESCE((SELECT SUM(amount) FROM payments WHERE customerId = c.id AND saleId IS NULL), 0.0)) AS totalDue
        FROM customers c
        LEFT JOIN sales s ON s.customerId = c.id AND s.status = 'COMPLETED'
        WHERE c.id = :customerId
        GROUP BY c.id
    """)
    fun getCustomerBalance(customerId: Long): Flow<CustomerWithBalance?>

    @Query("""
        SELECT COALESCE(SUM(totalDue), 0.0) FROM (
            SELECT 
                COALESCE(SUM(s.totalAmount), 0.0) - (COALESCE(SUM(s.paidAmount), 0.0) + COALESCE((SELECT SUM(amount) FROM payments WHERE customerId = c.id AND saleId IS NULL), 0.0)) AS totalDue
            FROM customers c
            LEFT JOIN sales s ON s.customerId = c.id AND s.status = 'COMPLETED'
            GROUP BY c.id
        ) WHERE totalDue > 0
    """)
    fun getTotalOutstandingDues(): Flow<Double>
}

