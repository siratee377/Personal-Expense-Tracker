package com.example.personal_expense_tracker.data.remote.api

import com.example.personal_expense_tracker.data.remote.request.SyncRequest
import com.example.personal_expense_tracker.data.remote.response.SyncResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ExpenseApi {
    @POST("expenses/sync")
    suspend fun sync(@Body request: SyncRequest): SyncResponse
}
