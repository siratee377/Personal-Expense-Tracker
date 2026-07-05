package com.example.personal_expense_tracker.data.local.entity

import androidx.room.Entity

@Entity(tableName = "expenses", primaryKeys = ["id"])
data class ExpenseEntity(
    val id: String,
    val title: String,
    val amountCents: Long,
    val category: String,
    val spentAt: Long,
    val note: String,
    val receiptPath: String?,
    val updatedAt: Long,
    val syncState: String,
    val deleted: Boolean,
)
