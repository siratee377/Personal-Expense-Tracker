package com.example.personal_expense_tracker.data.remote.dto

data class SyncOperationDto(
    val operation: String,
    val expense: ExpenseDto,
)
