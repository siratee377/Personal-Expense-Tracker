package com.example.personal_expense_tracker.domain.model

import com.example.personal_expense_tracker.domain.enum.ExpenseCategory

data class MonthlySummary(
    val totalCents: Long = 0,
    val transactionCount: Int = 0,
    val byCategory: Map<ExpenseCategory, Long> = emptyMap(),
)
