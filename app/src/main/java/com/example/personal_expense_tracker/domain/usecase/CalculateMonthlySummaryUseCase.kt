package com.example.personal_expense_tracker.domain.usecase

import com.example.personal_expense_tracker.domain.model.Expense
import com.example.personal_expense_tracker.domain.model.MonthlySummary
import java.util.Calendar

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
