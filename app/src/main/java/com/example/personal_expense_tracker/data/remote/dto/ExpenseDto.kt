package com.example.personal_expense_tracker.data.remote.dto

data class ExpenseDto(
    val id: String,
    val title: String,
    val amountCents: Long,
    val category: String,
    val spentAt: Long,
    val note: String,
    val receiptBase64: String?,
    val updatedAt: Long,
    val deleted: Boolean,
)
