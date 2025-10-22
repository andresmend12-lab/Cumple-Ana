package com.example.birthday.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.example.birthday.data.model.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY `order` ASC")
    fun observeActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    fun observeActivity(id: Int): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivity(id: Int): ActivityEntity?

    @Update
    suspend fun update(activity: ActivityEntity)

    @Query("SELECT * FROM activities WHERE isCompleted = 0 ORDER BY `order` ASC LIMIT 1")
    suspend fun getNextPending(): ActivityEntity?
}
