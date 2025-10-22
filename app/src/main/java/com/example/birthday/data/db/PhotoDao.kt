package com.example.birthday.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.birthday.data.model.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Query("SELECT * FROM photos WHERE activityId = :activityId ORDER BY createdAt ASC")
    fun observePhotos(activityId: Int): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos ORDER BY activityId ASC, createdAt ASC")
    fun observeAllPhotos(): Flow<List<PhotoEntity>>
}
