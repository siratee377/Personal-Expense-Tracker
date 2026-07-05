package com.example.personal_expense_tracker.domain.enum

enum class ExpenseCategory(val label: String, val emoji: String) {
    FOOD("Food", "F"),
    TRANSPORT("Transport", "T"),
    SHOPPING("Shopping", "S"),
    BILLS("Bills", "B"),
    HEALTH("Health", "H"),
    ENTERTAINMENT("Fun", "E"),
    OTHER("Other", "O"),
}
