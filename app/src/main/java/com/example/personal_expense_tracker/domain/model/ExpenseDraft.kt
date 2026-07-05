package com.example.personal_expense_tracker.domain.model

import com.example.personal_expense_tracker.domain.enum.ExpenseCategory

data class ExpenseDraft(
    val title: String,
    val amountCents: Long,
    val category: ExpenseCategory,
    val spentAt: Long,
    val note: String,
    val receiptPath: String?,
)
