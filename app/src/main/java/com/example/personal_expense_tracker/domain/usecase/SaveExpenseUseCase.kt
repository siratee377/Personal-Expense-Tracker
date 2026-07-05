package com.example.personal_expense_tracker.domain.usecase

import com.example.personal_expense_tracker.domain.model.ExpenseDraft
import com.example.personal_expense_tracker.domain.repository.ExpenseRepository

class SaveExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: String?, draft: ExpenseDraft) {
        require(draft.title.isNotBlank()) { "A title is required" }
        require(draft.amountCents > 0) { "Amount must be greater than zero" }
        repository.saveExpense(id, draft)
    }
}
