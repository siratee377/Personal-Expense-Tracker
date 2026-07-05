package com.example.personal_expense_tracker.data.local.entity

import androidx.room.Entity

@Entity(tableName = "sync_queue", primaryKeys = ["expenseId"])
data class SyncQueueEntity(
    val expenseId: String,
    val operation: String,
    val queuedAt: Long,
)
