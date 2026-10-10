package com.eta.assistant.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val saleId: Long? = null,
    val amount: Double,
    val paymentMethod: String = "CASH", // CASH, UPI, BANK
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "LOCAL"
)

