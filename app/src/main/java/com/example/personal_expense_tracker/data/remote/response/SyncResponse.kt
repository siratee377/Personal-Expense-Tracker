package com.example.personal_expense_tracker.data.remote.response

import com.example.personal_expense_tracker.data.remote.dto.ExpenseDto

data class SyncResponse(
    val acceptedIds: List<String>,
    val serverChanges: List<ExpenseDto>,
)
