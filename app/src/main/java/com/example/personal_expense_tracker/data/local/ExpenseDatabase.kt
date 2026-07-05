package com.example.personal_expense_tracker.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "expenses", primaryKeys = ["id"])
data class ExpenseEntity(
    val id: String,
    val title: String,
    val amountCents: Long,
    val category: String,
    val spentAt: Long,
    val note: String,
    val receiptPath: String?,
    val updatedAt: Long,
    val syncState: String,
    val deleted: Boolean,
)

@Entity(tableName = "sync_queue", primaryKeys = ["expenseId"])
data class SyncQueueEntity(
    val expenseId: String,
    val operation: String,
    val queuedAt: Long,
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE deleted = 0 AND (:query = '' OR title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY spentAt DESC")
    fun observeExpenses(query: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpense(id: String): ExpenseEntity?

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM sync_queue ORDER BY queuedAt")
    suspend fun getQueue(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE expenseId IN (:ids)")
    suspend fun removeQueued(ids: List<String>)

    @Query("UPDATE expenses SET syncState = :state WHERE id IN (:ids)")
    suspend fun updateSyncState(ids: List<String>, state: String)

    @Query("UPDATE expenses SET syncState = :state WHERE id IN (SELECT expenseId FROM sync_queue)")
    suspend fun updateQueuedSyncState(state: String)

    @Query("DELETE FROM expenses WHERE deleted = 1 AND id NOT IN (SELECT expenseId FROM sync_queue)")
    suspend fun purgeSyncedDeletes()

    @Transaction
    suspend fun saveAndQueue(expense: ExpenseEntity, operation: String) {
        upsertExpense(expense)
        enqueue(SyncQueueEntity(expense.id, operation, expense.updatedAt))
    }
}

@Database(entities = [ExpenseEntity::class, SyncQueueEntity::class], version = 1, exportSchema = true)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
