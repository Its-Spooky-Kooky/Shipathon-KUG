package com.eta.assistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String = "",
    val category: String = "Groceries",
    val unit: String = "pcs",
    val costPrice: Double = 0.0,
    val sellingPrice: Double,
    val stockQuantity: Double,
    val lowStockThreshold: Double = 5.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "LOCAL" // LOCAL, SYNCED, PENDING
)

