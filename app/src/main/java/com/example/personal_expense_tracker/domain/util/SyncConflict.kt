package com.example.personal_expense_tracker.domain.util

fun shouldApplyRemoteChange(localUpdatedAt: Long?, hasPendingLocalChange: Boolean, remoteUpdatedAt: Long): Boolean =
    !hasPendingLocalChange && (localUpdatedAt == null || remoteUpdatedAt > localUpdatedAt)
