package com.example.personal_expense_tracker.data.storage

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptStore @Inject constructor(@param:ApplicationContext private val context: Context) {
    fun copy(uriString: String): String {
        val directory = File(context.filesDir, "receipts").apply { mkdirs() }
        val target = File(directory, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uriString.toUri()).use { input ->
            requireNotNull(input) { "Could not open receipt" }
            target.outputStream().use(input::copyTo)
        }
        return target.absolutePath
    }
}
