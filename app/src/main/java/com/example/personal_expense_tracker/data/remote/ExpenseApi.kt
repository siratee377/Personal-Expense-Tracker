package com.example.personal_expense_tracker.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

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

data class SyncOperationDto(val operation: String, val expense: ExpenseDto)
data class SyncRequest(val deviceId: String, val changes: List<SyncOperationDto>)
data class SyncResponse(val acceptedIds: List<String>, val serverChanges: List<ExpenseDto>)

interface ExpenseApi {
    @POST("expenses/sync")
    suspend fun sync(@Body request: SyncRequest): SyncResponse
}
