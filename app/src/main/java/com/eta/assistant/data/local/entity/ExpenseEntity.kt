package com.eta.assistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "General",
    val amount: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "LOCAL"
)

