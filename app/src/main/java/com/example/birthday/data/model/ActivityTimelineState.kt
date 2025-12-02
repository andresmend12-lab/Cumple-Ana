package com.example.birthday.data.model

import java.time.Duration
import java.time.ZonedDateTime

enum class ActivityTimelineStatus {
    BLOCKED_TIME,
    PENDING_PHOTO,
    AVAILABLE,
    COMPLETED
}

sealed class ActivityLockReason {
    data class WaitingTime(val unlockAt: ZonedDateTime) : ActivityLockReason()
    object MissingPhoto : ActivityLockReason()
    object PreviousIncomplete : ActivityLockReason()
}

data class ActivityTimelineState(
    val activity: ActivityEntity,
    val status: ActivityTimelineStatus,
    val isAvailable: Boolean,
    val unlockAt: ZonedDateTime,
    val timeRemaining: Duration?,
    val lockReason: ActivityLockReason?,
    val hasPhoto: Boolean,
    val previousCompleted: Boolean
)
