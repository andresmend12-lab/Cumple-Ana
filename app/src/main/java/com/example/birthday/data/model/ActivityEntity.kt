package com.example.birthday.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val order: Int,
    val isCompleted: Boolean
)
