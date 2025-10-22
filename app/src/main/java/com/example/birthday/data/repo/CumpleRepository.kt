package com.example.birthday.data.repo

import com.example.birthday.data.db.ActivityDao
import com.example.birthday.data.db.PhotoDao
import com.example.birthday.data.db.VideoDao
import com.example.birthday.data.model.ActivityEntity
import com.example.birthday.data.model.PhotoEntity
import com.example.birthday.data.model.VideoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CumpleRepository(
    private val activityDao: ActivityDao,
    private val photoDao: PhotoDao,
    private val videoDao: VideoDao
) {
    fun observeActivities(): Flow<List<ActivityEntity>> = activityDao.observeActivities()

    fun observeActivity(activityId: Int): Flow<ActivityEntity?> = activityDao.observeActivity(activityId)

    fun observePhotos(activityId: Int): Flow<List<PhotoEntity>> = photoDao.observePhotos(activityId)

    fun observeAllPhotos(): Flow<List<PhotoEntity>> = photoDao.observeAllPhotos()

    fun observeFinalVideo(): Flow<VideoEntity?> = videoDao.observeFinalVideo()

    suspend fun addPhoto(activityId: Int, uri: String, createdAt: Long) {
        photoDao.insert(
            PhotoEntity(
                activityId = activityId,
                uri = uri,
                createdAt = createdAt
            )
        )
    }

    suspend fun markActivityCompleted(activityId: Int) {
        val entity = activityDao.getActivity(activityId) ?: return
        if (!entity.isCompleted) {
            activityDao.update(entity.copy(isCompleted = true))
        }
    }

    suspend fun isActivityCompleted(activityId: Int): Boolean {
        return activityDao.getActivity(activityId)?.isCompleted == true
    }

    suspend fun getFirstIncompleteActivityId(): Int? {
        return activityDao.getNextPending()?.id
    }

    suspend fun storeFinalVideo(uri: String, createdAt: Long) {
        videoDao.insert(
            VideoEntity(
                uri = uri,
                createdAt = createdAt,
                isFinal = true
            )
        )
    }

    suspend fun hasPhotoForActivity(activityId: Int): Boolean {
        return observePhotos(activityId).map { it.isNotEmpty() }.first()
    }
}
