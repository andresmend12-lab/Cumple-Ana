package com.example.birthday.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.birthday.data.model.ActivitySeeds
import java.util.concurrent.TimeUnit

object ActivityUnlockScheduler {
    private fun workName(activityId: Int) = "activity_unlock_${'$'}activityId"

    fun scheduleAll(context: Context, seeds: List<ActivitySeeds.Seed> = ActivitySeeds.activities) {
        val workManager = WorkManager.getInstance(context)
        seeds.forEach { seed ->
            val delayMillis = seed.unlockAt.toInstant().toEpochMilli() - System.currentTimeMillis()
            val clampedDelay = delayMillis.coerceAtLeast(0)
            val request = OneTimeWorkRequestBuilder<ActivityUnlockWorker>()
                .setInitialDelay(clampedDelay, TimeUnit.MILLISECONDS)
                .setInputData(ActivityUnlockWorker.inputData(seed.id))
                .addTag(workName(seed.id))
                .build()
            workManager.enqueueUniqueWork(workName(seed.id), ExistingWorkPolicy.REPLACE, request)
        }
    }
}
