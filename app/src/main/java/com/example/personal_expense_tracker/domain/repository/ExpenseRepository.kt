package com.example.personal_expense_tracker.domain.repository

import com.example.personal_expense_tracker.domain.model.Expense
import com.example.personal_expense_tracker.domain.model.ExpenseDraft
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(query: String): Flow<List<Expense>>
    suspend fun getExpense(id: String): Expense?
    suspend fun saveExpense(id: String?, draft: ExpenseDraft)
    suspend fun deleteExpense(id: String)
    suspend fun syncPending()
}
