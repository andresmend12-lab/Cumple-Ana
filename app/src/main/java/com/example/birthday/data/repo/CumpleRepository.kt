package com.example.birthday.data.repo

import com.example.birthday.data.db.ActivityDao
import com.example.birthday.data.db.PhotoDao
import com.example.birthday.data.model.ActivityCompletionResult
import com.example.birthday.data.model.ActivityEntity
import com.example.birthday.data.model.ActivityLockReason
import com.example.birthday.data.model.ActivityTimelineState
import com.example.birthday.data.model.ActivityTimelineStatus
import com.example.birthday.data.model.PhotoEntity
import com.example.birthday.util.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class CumpleRepository(
    private val activityDao: ActivityDao,
    private val photoDao: PhotoDao
) {
    fun observeActivities(): Flow<List<ActivityEntity>> = activityDao.observeActivities()

    fun observeActivity(activityId: Int): Flow<ActivityEntity?> = activityDao.observeActivity(activityId)

    fun observePhotos(activityId: Int): Flow<List<PhotoEntity>> = photoDao.observePhotos(activityId)

    fun observeAllPhotos(): Flow<List<PhotoEntity>> = photoDao.observeAllPhotos()

    fun observeTimelineState(zoneId: ZoneId = TimeUtils.zoneId): Flow<List<ActivityTimelineState>> {
        return combine(activityDao.observeActivities(), tickerFlow()) { activities, _ ->
            val now = TimeUtils.now(zoneId)
            buildTimelineStates(activities, now, zoneId)
        }
    }

    suspend fun addPhoto(activityId: Int, uri: String, createdAt: Long) {
        photoDao.insert(
            PhotoEntity(
                activityId = activityId,
                uri = uri,
                createdAt = createdAt
            )
        )
        markPhotoCompleted(activityId, TimeUtils.now())
    }

    suspend fun markPhotoCompleted(activityId: Int, now: ZonedDateTime = TimeUtils.now()) {
        val activities = activityDao.getActivities().sortedBy { it.order }
        val activity = activities.firstOrNull { it.id == activityId } ?: return
        val previousCompleted = activities
            .filter { it.order < activity.order }
            .all { it.isCompleted }
        val unlockAt = Instant.ofEpochMilli(activity.unlockAtEpochMillis).atZone(TimeUtils.zoneId)
        val shouldUnlock = previousCompleted && !now.isBefore(unlockAt)
        val updated = activity.copy(
            photoCompleted = true,
            isUnlocked = activity.isUnlocked || shouldUnlock
        )
        if (updated != activity) {
            activityDao.update(updated)
        } else if (!activity.photoCompleted) {
            activityDao.update(activity.copy(photoCompleted = true))
        }
    }

    suspend fun tryCompleteActivity(activityId: Int, now: ZonedDateTime = TimeUtils.now()): ActivityCompletionResult {
        val activities = activityDao.getActivities().sortedBy { it.order }
        val activity = activities.firstOrNull { it.id == activityId } ?: return ActivityCompletionResult.NotFound
        if (!activity.photoCompleted) {
            return ActivityCompletionResult.PhotoMissing
        }
        val previousCompleted = activities.filter { it.order < activity.order }.all { it.isCompleted }
        if (!previousCompleted) {
            return ActivityCompletionResult.PreviousIncomplete
        }
        val unlockAt = Instant.ofEpochMilli(activity.unlockAtEpochMillis).atZone(TimeUtils.zoneId)
        if (now.isBefore(unlockAt)) {
            val remaining = Duration.between(now, unlockAt)
            return ActivityCompletionResult.WaitingTime(unlockAt, remaining)
        }

        val updatedActivity = activity.copy(isCompleted = true, isUnlocked = true, photoCompleted = true)
        activityDao.update(updatedActivity)

        val nextActivity = activities.firstOrNull { it.order == activity.order + 1 }
        if (nextActivity != null) {
            val nextUnlock = Instant.ofEpochMilli(nextActivity.unlockAtEpochMillis).atZone(TimeUtils.zoneId)
            val shouldUnlockNext = !now.isBefore(nextUnlock)
            if (shouldUnlockNext && !nextActivity.isUnlocked) {
                activityDao.update(nextActivity.copy(isUnlocked = true))
            }
        }

        return ActivityCompletionResult.Completed(isFinal = nextActivity == null)
    }

    suspend fun isActivityCompleted(activityId: Int): Boolean {
        return activityDao.getActivity(activityId)?.isCompleted == true
    }

    suspend fun getFirstIncompleteActivityId(): Int? {
        return activityDao.getNextPending()?.id
    }

    suspend fun hasPhotoForActivity(activityId: Int): Boolean {
        return observePhotos(activityId).map { it.isNotEmpty() }.first()
    }

    private fun buildTimelineStates(
        activities: List<ActivityEntity>,
        now: ZonedDateTime,
        zoneId: java.time.ZoneId
    ): List<ActivityTimelineState> {
        val sorted = activities.sortedBy { it.order }
        var previousCompleted = true
        return sorted.map { activity ->
            val unlockAt = Instant.ofEpochMilli(activity.unlockAtEpochMillis).atZone(zoneId)
            val isTimeReached = !now.isBefore(unlockAt)
            val hasPhoto = activity.photoCompleted
            val prevCompletedForThis = previousCompleted
            val status: ActivityTimelineStatus
            val isAvailable: Boolean
            val lockReason: ActivityLockReason?
            val timeRemaining = if (!isTimeReached) Duration.between(now, unlockAt) else null

            if (activity.isCompleted) {
                status = ActivityTimelineStatus.COMPLETED
                isAvailable = true
                lockReason = null
            } else if (!prevCompletedForThis) {
                status = ActivityTimelineStatus.PENDING_PHOTO
                isAvailable = false
                lockReason = ActivityLockReason.PreviousIncomplete
            } else if (!isTimeReached) {
                status = ActivityTimelineStatus.BLOCKED_TIME
                isAvailable = false
                lockReason = ActivityLockReason.WaitingTime(unlockAt)
            } else if (!hasPhoto) {
                status = ActivityTimelineStatus.PENDING_PHOTO
                isAvailable = true
                lockReason = ActivityLockReason.MissingPhoto
            } else {
                status = ActivityTimelineStatus.AVAILABLE
                isAvailable = true
                lockReason = null
            }

            val state = ActivityTimelineState(
                activity = activity,
                status = status,
                isAvailable = isAvailable,
                unlockAt = unlockAt,
                timeRemaining = timeRemaining,
                lockReason = lockReason,
                hasPhoto = hasPhoto,
                previousCompleted = prevCompletedForThis
            )

            previousCompleted = previousCompleted && activity.isCompleted
            state
        }
    }

    private fun tickerFlow(periodMillis: Long = 1_000L): Flow<Long> = flow {
        emit(System.currentTimeMillis())
        while (true) {
            delay(periodMillis)
            emit(System.currentTimeMillis())
        }
    }
}
