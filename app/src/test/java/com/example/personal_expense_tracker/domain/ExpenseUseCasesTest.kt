package com.example.personal_expense_tracker.domain

import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseUseCasesTest {
    @Test
    fun `save rejects zero amount`() = runTest {
        val repository = FakeRepository()
        val result = runCatching {
            SaveExpenseUseCase(repository)(null, draft(amountCents = 0))
        }
        assertTrue(result.isFailure)
        assertEquals(0, repository.saved.size)
    }

    @Test
    fun `monthly summary ignores expenses outside selected month`() {
        val july = date(2026, Calendar.JULY, 4)
        val expenses = listOf(
            expense("1", 1_250, ExpenseCategory.FOOD, july),
            expense("2", 750, ExpenseCategory.FOOD, july),
            expense("3", 9_999, ExpenseCategory.BILLS, date(2026, Calendar.JUNE, 30)),
        )

        val summary = CalculateMonthlySummaryUseCase()(expenses, july)

        assertEquals(2_000, summary.totalCents)
        assertEquals(2, summary.transactionCount)
        assertEquals(2_000L, summary.byCategory[ExpenseCategory.FOOD])
    }

    @Test
    fun `pending local change wins conflict even when remote is newer`() {
        assertFalse(shouldApplyRemoteChange(localUpdatedAt = 100, hasPendingLocalChange = true, remoteUpdatedAt = 200))
        assertTrue(shouldApplyRemoteChange(localUpdatedAt = 100, hasPendingLocalChange = false, remoteUpdatedAt = 200))
    }

    private fun draft(amountCents: Long) = ExpenseDraft("Lunch", amountCents, ExpenseCategory.FOOD, 0, "", null)

    private fun expense(id: String, amount: Long, category: ExpenseCategory, date: Long) = Expense(
        id, "Expense", amount, category, date, updatedAt = date, syncState = SyncState.SYNCED,
    )

    private fun date(year: Int, month: Int, day: Int) = Calendar.getInstance().apply {
        clear(); set(year, month, day)
    }.timeInMillis

    private class FakeRepository : ExpenseRepository {
        val saved = mutableListOf<ExpenseDraft>()
        override fun observeExpenses(query: String): Flow<List<Expense>> = flowOf(emptyList())
        override suspend fun getExpense(id: String): Expense? = null
        override suspend fun saveExpense(id: String?, draft: ExpenseDraft) { saved += draft }
        override suspend fun deleteExpense(id: String) = Unit
        override suspend fun syncPending() = Unit
    }
}
