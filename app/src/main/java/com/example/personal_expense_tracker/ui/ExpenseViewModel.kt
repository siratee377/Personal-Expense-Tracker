package com.example.personal_expense_tracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personal_expense_tracker.data.network.NetworkMonitor
import com.example.personal_expense_tracker.data.storage.ReceiptStore
import com.example.personal_expense_tracker.domain.enum.ExpenseCategory
import com.example.personal_expense_tracker.domain.model.Expense
import com.example.personal_expense_tracker.domain.model.ExpenseDraft
import com.example.personal_expense_tracker.domain.model.MonthlySummary
import com.example.personal_expense_tracker.domain.repository.ExpenseRepository
import com.example.personal_expense_tracker.domain.usecase.CalculateMonthlySummaryUseCase
import com.example.personal_expense_tracker.domain.usecase.DeleteExpenseUseCase
import com.example.personal_expense_tracker.domain.usecase.SaveExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpenseFormState(
    val id: String? = null,
    val title: String = "",
    val amount: String = "",
    val category: ExpenseCategory = ExpenseCategory.FOOD,
    val spentAt: Long = System.currentTimeMillis(),
    val note: String = "",
    val receiptPath: String? = null,
    val error: String? = null,
)

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val query: String = "",
    val summary: MonthlySummary = MonthlySummary(),
    val isOnline: Boolean = false,
    val showEditor: Boolean = false,
    val form: ExpenseFormState = ExpenseFormState(),
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val saveExpense: SaveExpenseUseCase,
    private val deleteExpense: DeleteExpenseUseCase,
    private val calculateMonthlySummary: CalculateMonthlySummaryUseCase,
    private val receiptStore: ReceiptStore,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val editor = MutableStateFlow(false)
    private val form = MutableStateFlow(ExpenseFormState())
    private val filtered = query.flatMapLatest(repository::observeExpenses)
    private val allExpenses = repository.observeExpenses("")

    private val content = combine(filtered, allExpenses, query) { visible, all, search ->
        Triple(visible, all, search)
    }

    val uiState = combine(content, networkMonitor.isOnline, editor, form) {
            (visible, all, search), online, showEditor, formState ->
        ExpenseUiState(visible, search, calculateMonthlySummary(all), online, showEditor, formState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseUiState())

    fun search(value: String) { query.value = value }
    fun add() { form.value = ExpenseFormState(); editor.value = true }
    fun closeEditor() { editor.value = false; form.value = ExpenseFormState() }
    fun updateForm(transform: (ExpenseFormState) -> ExpenseFormState) { form.value = transform(form.value) }

    fun edit(expense: Expense) {
        form.value = ExpenseFormState(
            expense.id, expense.title, BigDecimal(expense.amountCents).movePointLeft(2).toPlainString(),
            expense.category, expense.spentAt, expense.note, expense.receiptPath,
        )
        editor.value = true
    }

    fun attachReceipt(uri: String) = viewModelScope.launch {
        runCatching { receiptStore.copy(uri) }
            .onSuccess { path -> updateForm { it.copy(receiptPath = path, error = null) } }
            .onFailure { error -> updateForm { it.copy(error = error.message ?: "Could not attach receipt") } }
    }

    fun save() = viewModelScope.launch {
        val state = form.value
        val cents = runCatching {
            BigDecimal(state.amount).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()
        }.getOrNull()
        if (cents == null) {
            form.value = state.copy(error = "Enter a valid amount")
            return@launch
        }
        runCatching {
            saveExpense(state.id, ExpenseDraft(state.title, cents, state.category, state.spentAt, state.note, state.receiptPath))
        }.onSuccess { closeEditor() }
            .onFailure { error -> form.value = state.copy(error = error.message) }
    }

    fun delete(id: String) = viewModelScope.launch { deleteExpense(id) }

}
