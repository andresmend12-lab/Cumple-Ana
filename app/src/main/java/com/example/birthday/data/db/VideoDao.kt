package com.example.birthday.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.birthday.data.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert
    suspend fun insert(video: VideoEntity): Long

    @Query("SELECT * FROM videos WHERE isFinal = 1 ORDER BY createdAt DESC LIMIT 1")
    fun observeFinalVideo(): Flow<VideoEntity?>
}
