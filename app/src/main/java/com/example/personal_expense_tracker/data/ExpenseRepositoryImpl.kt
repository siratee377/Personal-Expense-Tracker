package com.example.personal_expense_tracker.data

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import androidx.room.withTransaction
import com.example.personal_expense_tracker.data.local.ExpenseDao
import com.example.personal_expense_tracker.data.local.ExpenseDatabase
import com.example.personal_expense_tracker.data.local.ExpenseEntity
import com.example.personal_expense_tracker.data.remote.ExpenseApi
import com.example.personal_expense_tracker.data.remote.ExpenseDto
import com.example.personal_expense_tracker.data.remote.SyncOperationDto
import com.example.personal_expense_tracker.data.remote.SyncRequest
import com.example.personal_expense_tracker.domain.Expense
import com.example.personal_expense_tracker.domain.ExpenseCategory
import com.example.personal_expense_tracker.domain.ExpenseDraft
import com.example.personal_expense_tracker.domain.ExpenseRepository
import com.example.personal_expense_tracker.domain.SyncState
import com.example.personal_expense_tracker.domain.shouldApplyRemoteChange
import com.example.personal_expense_tracker.sync.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val database: ExpenseDatabase,
    private val dao: ExpenseDao,
    private val api: ExpenseApi,
    private val syncScheduler: SyncScheduler,
    @param:ApplicationContext private val context: Context,
) : ExpenseRepository {

    override fun observeExpenses(query: String): Flow<List<Expense>> =
        dao.observeExpenses(query.trim()).map { rows -> rows.map(ExpenseEntity::toDomain) }

    override suspend fun getExpense(id: String): Expense? = dao.getExpense(id)?.toDomain()

    override suspend fun saveExpense(id: String?, draft: ExpenseDraft) {
        val now = System.currentTimeMillis()
        val previousReceipt = id?.let { dao.getExpense(it)?.receiptPath }
        val entity = ExpenseEntity(
            id = id ?: UUID.randomUUID().toString(),
            title = draft.title.trim(),
            amountCents = draft.amountCents,
            category = draft.category.name,
            spentAt = draft.spentAt,
            note = draft.note.trim(),
            receiptPath = draft.receiptPath,
            updatedAt = now,
            syncState = SyncState.PENDING.name,
            deleted = false,
        )
        dao.saveAndQueue(entity, "UPSERT")
        if (previousReceipt != null && previousReceipt != draft.receiptPath) File(previousReceipt).delete()
        syncScheduler.schedule()
    }

    override suspend fun deleteExpense(id: String) {
        val existing = dao.getExpense(id) ?: return
        val deleted = existing.copy(
            deleted = true,
            updatedAt = System.currentTimeMillis(),
            syncState = SyncState.PENDING.name,
        )
        dao.saveAndQueue(deleted, "DELETE")
        syncScheduler.schedule()
    }

    override suspend fun syncPending() {
        database.withTransaction {
            dao.updateQueuedSyncState(SyncState.PENDING.name)
        }
        val queued = dao.getQueue()
        if (queued.isEmpty()) return
        val localById = dao.getAllExpenses().associateBy { it.id }
        val changes = queued.mapNotNull { item ->
            localById[item.expenseId]?.let { SyncOperationDto(item.operation, it.toDto()) }
        }
        val response = try {
            api.sync(SyncRequest(deviceId(), changes))
        } catch (error: Exception) {
            dao.updateQueuedSyncState(SyncState.FAILED.name)
            throw error
        }
        val pendingIds = queued.map { it.expenseId }.toSet()
        response.acceptedIds.forEach { id ->
            localById[id]?.takeIf { it.deleted }?.receiptPath?.let { File(it).delete() }
        }
        database.withTransaction {
            response.serverChanges.forEach { remote ->
                val local = dao.getExpense(remote.id)
                if (shouldApplyRemoteChange(local?.updatedAt, remote.id in pendingIds, remote.updatedAt)) {
                    dao.upsertExpense(remote.toEntity())
                }
            }
            if (response.acceptedIds.isNotEmpty()) {
                dao.updateSyncState(response.acceptedIds, SyncState.SYNCED.name)
                dao.removeQueued(response.acceptedIds)
                dao.purgeSyncedDeletes()
            }
        }
    }

    private fun deviceId(): String {
        val prefs = context.getSharedPreferences("sync", Context.MODE_PRIVATE)
        return prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit { putString("device_id", it) }
        }
    }

    private fun ExpenseEntity.toDto(): ExpenseDto = ExpenseDto(
        id, title, amountCents, category, spentAt, note,
        receiptPath?.let { path -> File(path).takeIf(File::exists)?.readBytes()?.let { Base64.encodeToString(it, Base64.NO_WRAP) } },
        updatedAt, deleted,
    )

    private fun ExpenseDto.toEntity() = ExpenseEntity(
        id, title, amountCents, category, spentAt, note, null, updatedAt, SyncState.SYNCED.name, deleted,
    )
}

private fun ExpenseEntity.toDomain() = Expense(
    id = id,
    title = title,
    amountCents = amountCents,
    category = ExpenseCategory.entries.firstOrNull { it.name == category } ?: ExpenseCategory.OTHER,
    spentAt = spentAt,
    note = note,
    receiptPath = receiptPath,
    updatedAt = updatedAt,
    syncState = SyncState.entries.firstOrNull { it.name == syncState } ?: SyncState.PENDING,
)
