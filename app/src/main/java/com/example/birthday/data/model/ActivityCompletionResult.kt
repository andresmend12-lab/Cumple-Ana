package com.example.birthday.data.model

import java.time.Duration
import java.time.ZonedDateTime

sealed class ActivityCompletionResult {
    data class Completed(val isFinal: Boolean) : ActivityCompletionResult()
    data class WaitingTime(val unlockAt: ZonedDateTime, val remaining: Duration) : ActivityCompletionResult()
    object PreviousIncomplete : ActivityCompletionResult()
    object PhotoMissing : ActivityCompletionResult()
    object NotFound : ActivityCompletionResult()
}
