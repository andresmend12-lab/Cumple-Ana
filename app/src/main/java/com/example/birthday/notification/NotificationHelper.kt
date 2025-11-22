package com.example.birthday.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.birthday.MainActivity
import com.example.birthday.R
import com.example.birthday.data.model.ActivityEntity

object NotificationHelper {
    const val CHANNEL_ID = "activity_unlocks"
    const val EXTRA_ACTIVITY_ID = "extra_activity_id"
    const val EXTRA_OPEN_TIMELINE = "extra_open_timeline"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_activities),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_activities_desc)
                }
                manager?.createNotificationChannel(channel)
            }
        }
    }

    fun showUnlockNotification(
        context: Context,
        activity: ActivityEntity,
        previousCompleted: Boolean
    ) {
        ensureChannel(context)
        val pendingIntent = createPendingIntent(context, activity.id, previousCompleted)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cake)
            .setContentTitle(context.getString(R.string.notification_unlock_title))
            .setContentText(context.getString(R.string.notification_unlock_body, activity.title))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        NotificationManagerCompat.from(context).notify(activity.id, notification)
    }

    private fun createPendingIntent(
        context: Context,
        activityId: Int,
        openDirectly: Boolean
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (openDirectly) {
                putExtra(EXTRA_ACTIVITY_ID, activityId)
            } else {
                putExtra(EXTRA_OPEN_TIMELINE, true)
            }
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, activityId, intent, flags)
    }
}
