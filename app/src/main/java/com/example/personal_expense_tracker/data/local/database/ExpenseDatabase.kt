package com.example.personal_expense_tracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.personal_expense_tracker.data.local.dao.ExpenseDao
import com.example.personal_expense_tracker.data.local.entity.ExpenseEntity
import com.example.personal_expense_tracker.data.local.entity.SyncQueueEntity

@Database(entities = [ExpenseEntity::class, SyncQueueEntity::class], version = 1, exportSchema = true)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
