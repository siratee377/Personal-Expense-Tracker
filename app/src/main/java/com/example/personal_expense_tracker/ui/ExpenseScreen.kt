package com.example.personal_expense_tracker.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personal_expense_tracker.domain.enum.ExpenseCategory
import com.example.personal_expense_tracker.domain.enum.SyncState
import com.example.personal_expense_tracker.domain.model.Expense
import com.example.personal_expense_tracker.domain.model.MonthlySummary
import java.io.File
import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

@Composable
fun ExpenseTrackerRoute(viewModel: ExpenseViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExpenseTrackerScreen(
        state = state,
        onSearch = viewModel::search,
        onAdd = viewModel::add,
        onEdit = viewModel::edit,
        onDelete = viewModel::delete,
        onCloseEditor = viewModel::closeEditor,
        onFormChange = viewModel::updateForm,
        onAttach = viewModel::attachReceipt,
        onSave = viewModel::save,
    )
}

@Composable
private fun ExpenseTrackerScreen(
    state: ExpenseUiState,
    onSearch: (String) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Expense) -> Unit,
    onDelete: (String) -> Unit,
    onCloseEditor: () -> Unit,
    onFormChange: ((ExpenseFormState) -> ExpenseFormState) -> Unit,
    onAttach: (String) -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Add expense") },
                elevation = FloatingActionButtonDefaults.elevation(2.dp),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 16.dp, 20.dp, 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Header(state.isOnline) }
            item { SummaryCard(state.summary) }
            item { CategoryChart(state.summary) }
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onSearch,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    placeholder = { Text("Search expenses, notes, categories") },
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${state.expenses.size} entries", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.expenses.isEmpty()) {
                item { EmptyState(hasQuery = state.query.isNotBlank(), onAdd = onAdd) }
            } else {
                items(state.expenses, key = Expense::id) { expense ->
                    ExpenseRow(expense, onEdit, onDelete)
                }
            }
        }
    }
    if (state.showEditor) {
        ExpenseEditor(state.form, onCloseEditor, onFormChange, onAttach, onSave)
    }
}

@Composable
private fun Header(isOnline: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("POCKET LEDGER", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Know where it went.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }
        AssistChip(
            onClick = {},
            label = { Text(if (isOnline) "Online" else "Offline") },
            leadingIcon = { Icon(if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff, null, Modifier.size(18.dp)) },
        )
    }
}

@Composable
private fun SummaryCard(summary: MonthlySummary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("SPENT THIS MONTH", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .72f), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Text(money(summary.totalCents), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(18.dp))
            Text("${summary.transactionCount} transactions · saved locally first", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .84f))
        }
    }
}

@Composable
private fun CategoryChart(summary: MonthlySummary) {
    val values = summary.byCategory.entries.sortedByDescending { it.value }.take(5)
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Monthly mix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            if (values.isEmpty()) {
                Text("Your category chart will grow here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val maximum = values.maxOf { it.value }.coerceAtLeast(1)
                values.forEach { (category, amount) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(category.label, modifier = Modifier.weight(.28f), maxLines = 1)
                        Canvas(Modifier.weight(.48f).height(12.dp)) {
                            drawRoundRect(Color(0xFFE4EBE3), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height))
                            drawRoundRect(Color(0xFFEF765A), size = size.copy(width = size.width * amount / maximum), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height))
                        }
                        Text(money(amount), modifier = Modifier.weight(.24f), textAlign = androidx.compose.ui.text.style.TextAlign.End, style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onEdit: (Expense) -> Unit, onDelete: (String) -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(categoryColor(expense.category)), contentAlignment = Alignment.Center) {
                Text(expense.category.emoji, fontWeight = FontWeight.Black)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(expense.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (expense.receiptPath != null) Icon(Icons.AutoMirrored.Rounded.ReceiptLong, "Receipt attached", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${expense.category.label} · ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(expense.spentAt))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (expense.syncState == SyncState.SYNCED) "Synced" else "Waiting to sync", style = MaterialTheme.typography.labelSmall, color = if (expense.syncState == SyncState.SYNCED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(money(expense.amountCents), fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = { onEdit(expense) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Rounded.Edit, "Edit", Modifier.size(18.dp)) }
                    IconButton(onClick = { onDelete(expense.id) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Rounded.DeleteOutline, "Delete", Modifier.size(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(hasQuery: Boolean, onAdd: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Rounded.ReceiptLong, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(if (hasQuery) "No matching expenses" else "Your ledger is ready", style = MaterialTheme.typography.titleMedium)
        if (!hasQuery) TextButton(onClick = onAdd) { Text("Record the first expense") }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExpenseEditor(
    form: ExpenseFormState,
    onDismiss: () -> Unit,
    onChange: ((ExpenseFormState) -> ExpenseFormState) -> Unit,
    onAttach: (String) -> Unit,
    onSave: () -> Unit,
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let { onAttach(it.toString()) } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (form.id == null) "New expense" else "Edit expense", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(form.title, { value -> onChange { it.copy(title = value, error = null) } }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(form.amount, { value -> onChange { it.copy(amount = value, error = null) } }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                }
                item {
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExpenseCategory.entries.forEach { category ->
                            FilterChip(selected = category == form.category, onClick = { onChange { it.copy(category = category) } }, label = { Text(category.label) })
                        }
                    }
                }
                item {
                    OutlinedTextField(form.note, { value -> onChange { it.copy(note = value) } }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
                item {
                    form.receiptPath?.let { ReceiptPreview(it) }
                    OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.AttachFile, null)
                        Text(if (form.receiptPath == null) "Attach receipt" else "Replace receipt")
                    }
                }
                form.error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save locally") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ReceiptPreview(path: String) {
    val bitmap = remember(path) { File(path).takeIf(File::exists)?.let { BitmapFactory.decodeFile(it.absolutePath) } }
    Image(
        bitmap = bitmap?.asImageBitmap() ?: return,
        contentDescription = "Receipt preview",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().aspectRatio(2.2f).clip(RoundedCornerShape(14.dp)),
    )
    Spacer(Modifier.height(8.dp))
}

private fun money(cents: Long): String = NumberFormat.getCurrencyInstance().format(BigDecimal(cents).movePointLeft(2))

private fun categoryColor(category: ExpenseCategory): Color = when (category) {
    ExpenseCategory.FOOD -> Color(0xFFFFD8B8)
    ExpenseCategory.TRANSPORT -> Color(0xFFC9E7E1)
    ExpenseCategory.SHOPPING -> Color(0xFFF5C7D3)
    ExpenseCategory.BILLS -> Color(0xFFD5DAF4)
    ExpenseCategory.HEALTH -> Color(0xFFCBE7C3)
    ExpenseCategory.ENTERTAINMENT -> Color(0xFFFFE6A8)
    ExpenseCategory.OTHER -> Color(0xFFE1DDD5)
}
