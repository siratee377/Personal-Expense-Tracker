package com.example.personal_expense_tracker.data.remote.request

import com.example.personal_expense_tracker.data.remote.dto.SyncOperationDto

data class SyncRequest(
    val deviceId: String,
    val changes: List<SyncOperationDto>,
)
