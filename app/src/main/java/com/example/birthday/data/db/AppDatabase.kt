package com.example.birthday.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.birthday.data.model.ActivityEntity
import com.example.birthday.data.model.ActivitySeeds
import com.example.birthday.data.model.PhotoEntity
import com.example.birthday.data.model.VideoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ActivityEntity::class, PhotoEntity::class, VideoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun photoDao(): PhotoDao
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "cumple_ana.db")
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            prepopulateActivities().forEach { entity ->
                                db.execSQL(
                                    "INSERT INTO activities(id, title, description, `order`, unlockAtEpochMillis, photoCompleted, isUnlocked, isCompleted) VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
                                    arrayOf(
                                        entity.id,
                                        entity.title,
                                        entity.description,
                                        entity.order,
                                        entity.unlockAtEpochMillis,
                                        if (entity.photoCompleted) 1 else 0,
                                        if (entity.isUnlocked) 1 else 0,
                                        if (entity.isCompleted) 1 else 0
                                    )
                                )
                            }
                        }
                    }
                })
                .build()
        }

        private fun prepopulateActivities(): List<ActivityEntity> =
            ActivitySeeds.activities.map { seed ->
                ActivityEntity(
                    id = seed.id,
                    order = seed.order,
                    title = seed.title,
                    description = seed.description,
                    unlockAtEpochMillis = seed.unlockAtEpochMillis,
                    photoCompleted = false,
                    isUnlocked = false,
                    isCompleted = false
                )
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN unlockAtEpochMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE activities ADD COLUMN photoCompleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE activities ADD COLUMN isUnlocked INTEGER NOT NULL DEFAULT 0")

                ActivitySeeds.activities.forEach { seed ->
                    db.execSQL(
                        "UPDATE activities SET title = ?, description = ?, `order` = ?, unlockAtEpochMillis = ? WHERE id = ?",
                        arrayOf(
                            seed.title,
                            seed.description,
                            seed.order,
                            seed.unlockAtEpochMillis,
                            seed.id
                        )
                    )
                }

                db.execSQL(
                    "UPDATE activities SET photoCompleted = 1 WHERE id IN (SELECT DISTINCT activityId FROM photos)"
                )
            }
        }
    }
}
