package com.example.personal_expense_tracker.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.personal_expense_tracker.data.ExpenseRepositoryImpl
import com.example.personal_expense_tracker.data.local.dao.ExpenseDao
import com.example.personal_expense_tracker.data.local.database.ExpenseDatabase
import com.example.personal_expense_tracker.data.remote.api.ExpenseApi
import com.example.personal_expense_tracker.domain.repository.ExpenseRepository
import com.example.personal_expense_tracker.domain.usecase.CalculateMonthlySummaryUseCase
import com.example.personal_expense_tracker.domain.usecase.DeleteExpenseUseCase
import com.example.personal_expense_tracker.domain.usecase.SaveExpenseUseCase
import com.example.personal_expense_tracker.sync.SyncScheduler
import com.example.personal_expense_tracker.sync.WorkManagerSyncScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {
    @Binds abstract fun bindRepository(implementation: ExpenseRepositoryImpl): ExpenseRepository
    @Binds abstract fun bindSyncScheduler(implementation: WorkManagerSyncScheduler): SyncScheduler
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Replace this with your demo backend. The app remains fully functional offline without it.
    private const val BASE_URL = "https://example.com/api/"

    @Provides @Singleton
    fun database(@ApplicationContext context: Context): ExpenseDatabase =
        Room.databaseBuilder(context, ExpenseDatabase::class.java, "expense-tracker.db").build()

    @Provides fun dao(database: ExpenseDatabase): ExpenseDao = database.expenseDao()
    @Provides fun workManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    @Provides @Singleton
    fun expenseApi(): ExpenseApi {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExpenseApi::class.java)
    }

    @Provides fun saveExpense(repository: ExpenseRepository) = SaveExpenseUseCase(repository)
    @Provides fun deleteExpense(repository: ExpenseRepository) = DeleteExpenseUseCase(repository)
    @Provides fun monthlySummary() = CalculateMonthlySummaryUseCase()
}
