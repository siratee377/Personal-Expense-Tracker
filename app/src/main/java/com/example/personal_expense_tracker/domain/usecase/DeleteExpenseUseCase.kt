package com.example.personal_expense_tracker.domain.usecase

import com.example.personal_expense_tracker.domain.repository.ExpenseRepository

class DeleteExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteExpense(id)
}
