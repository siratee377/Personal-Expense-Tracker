package com.example.personal_expense_tracker.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.personal_expense_tracker.domain.ExpenseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

interface SyncScheduler { fun schedule() }

@Singleton
class WorkManagerSyncScheduler @Inject constructor(
    private val workManager: WorkManager,
) : SyncScheduler {
    override fun schedule() {
        val request = OneTimeWorkRequestBuilder<ExpenseSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    private companion object { const val WORK_NAME = "expense-outbox-sync" }
}

@HiltWorker
class ExpenseSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ExpenseRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        repository.syncPending()
        Result.success()
    } catch (error: HttpException) {
        if (error.code() in 400..499) Result.failure() else Result.retry()
    } catch (_: IOException) {
        Result.retry()
    } catch (_: Exception) {
        Result.failure()
    }
}
