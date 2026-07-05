package com.example.personal_expense_tracker.domain.model

import com.example.personal_expense_tracker.domain.enum.ExpenseCategory
import com.example.personal_expense_tracker.domain.enum.SyncState

data class Expense(
    val id: String,
    val title: String,
    val amountCents: Long,
    val category: ExpenseCategory,
    val spentAt: Long,
    val note: String = "",
    val receiptPath: String? = null,
    val updatedAt: Long,
    val syncState: SyncState,
)
