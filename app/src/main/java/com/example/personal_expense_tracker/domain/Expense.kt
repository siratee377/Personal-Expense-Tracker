package com.example.personal_expense_tracker.domain

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

enum class ExpenseCategory(val label: String, val emoji: String) {
    FOOD("Food", "F"), TRANSPORT("Transport", "T"), SHOPPING("Shopping", "S"),
    BILLS("Bills", "B"), HEALTH("Health", "H"), ENTERTAINMENT("Fun", "E"), OTHER("Other", "O")
}

enum class SyncState { PENDING, SYNCED, FAILED }

data class Expense(
    val id: String,
    val title: String,
    val amountCents: Long,
    val category: ExpenseCategory,
    val spentAt: Long,
    val note: String = "",
    val receiptPath: String? = null,
    val updatedAt: Long,
    val syncState: SyncState,
)

data class ExpenseDraft(
    val title: String,
    val amountCents: Long,
    val category: ExpenseCategory,
    val spentAt: Long,
    val note: String,
    val receiptPath: String?,
)

data class MonthlySummary(
    val totalCents: Long = 0,
    val transactionCount: Int = 0,
    val byCategory: Map<ExpenseCategory, Long> = emptyMap(),
)

interface ExpenseRepository {
    fun observeExpenses(query: String): Flow<List<Expense>>
    suspend fun getExpense(id: String): Expense?
    suspend fun saveExpense(id: String?, draft: ExpenseDraft)
    suspend fun deleteExpense(id: String)
    suspend fun syncPending()
}

class SaveExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: String?, draft: ExpenseDraft) {
        require(draft.title.isNotBlank()) { "A title is required" }
        require(draft.amountCents > 0) { "Amount must be greater than zero" }
        repository.saveExpense(id, draft)
    }
}

class DeleteExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteExpense(id)
}

class CalculateMonthlySummaryUseCase {
    operator fun invoke(expenses: List<Expense>, now: Long = System.currentTimeMillis()): MonthlySummary {
        val currentDate = Calendar.getInstance().apply { timeInMillis = now }
        val current = expenses.filter { expense ->
            Calendar.getInstance().apply { timeInMillis = expense.spentAt }.let { date ->
                date.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                    date.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
            }
        }
        return MonthlySummary(
            totalCents = current.sumOf(Expense::amountCents),
            transactionCount = current.size,
            byCategory = current.groupBy(Expense::category).mapValues { (_, rows) -> rows.sumOf(Expense::amountCents) },
        )
    }
}

fun shouldApplyRemoteChange(localUpdatedAt: Long?, hasPendingLocalChange: Boolean, remoteUpdatedAt: Long): Boolean =
    !hasPendingLocalChange && (localUpdatedAt == null || remoteUpdatedAt > localUpdatedAt)
