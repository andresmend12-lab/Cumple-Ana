package com.example.birthday.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.birthday.data.db.AppDatabase

class ActivityUnlockWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val activityId = inputData.getInt(KEY_ACTIVITY_ID, -1)
        if (activityId <= 0) {
            return Result.success()
        }
        val database = AppDatabase.getInstance(applicationContext)
        val activityDao = database.activityDao()
        val activities = activityDao.getActivities().sortedBy { it.order }
        val activity = activities.firstOrNull { it.id == activityId } ?: return Result.success()
        val previousCompleted = activities.filter { it.order < activity.order }.all { it.isCompleted }
        val isFullyUnlocked = previousCompleted && activity.photoCompleted
        if (isFullyUnlocked && !activity.isUnlocked) {
            activityDao.update(activity.copy(isUnlocked = true))
        }
        val entityForNotification = if (isFullyUnlocked && !activity.isUnlocked) {
            activity.copy(isUnlocked = true)
        } else {
            activity
        }
        NotificationHelper.showUnlockNotification(applicationContext, entityForNotification, isFullyUnlocked)
        return Result.success()
    }

    companion object {
        const val KEY_ACTIVITY_ID = "key_activity_id"

        fun inputData(activityId: Int) = workDataOf(KEY_ACTIVITY_ID to activityId)
    }
}
