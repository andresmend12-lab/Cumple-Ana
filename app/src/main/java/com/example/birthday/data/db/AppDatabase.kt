package com.example.birthday.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.birthday.data.model.ActivityEntity
import com.example.birthday.data.model.PhotoEntity
import com.example.birthday.data.model.VideoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ActivityEntity::class, PhotoEntity::class, VideoEntity::class],
    version = 1,
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
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            prepopulateActivities().forEach { entity ->
                                db.execSQL(
                                    "INSERT INTO activities(id, title, description, `order`, isCompleted) VALUES(?, ?, ?, ?, ?)",
                                    arrayOf(
                                        entity.id,
                                        entity.title,
                                        entity.description,
                                        entity.order,
                                        if (entity.isCompleted) 1 else 0
                                    )
                                )
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
        }

        private fun prepopulateActivities(): List<ActivityEntity> = listOf(
            ActivityEntity(
                id = 1,
                order = 1,
                title = "Coronación",
                description = "Coloca la corona y sopla las velas de la primera tarta (00:00).",
                isCompleted = false
            ),
            ActivityEntity(
                id = 2,
                order = 2,
                title = "Desayuno",
                description = "Desayuno en la cama con bacon, café y flores.",
                isCompleted = false
            ),
            ActivityEntity(
                id = 3,
                order = 3,
                title = "Cuídate",
                description = "Primer regalo: libro o detalle de calma/autocuidado.",
                isCompleted = false
            ),
            ActivityEntity(
                id = 4,
                order = 4,
                title = "Que rico el cafesito",
                description = "Paseo o momento relax con café en tu sitio favorito.",
                isCompleted = false
            ),
            ActivityEntity(
                id = 5,
                order = 5,
                title = "Una tarta nunca es suficiente",
                description = "Merienda con tarta 3 leches (segunda tarta).",
                isCompleted = false
            ),
            ActivityEntity(
                id = 6,
                order = 6,
                title = "Siempre divina",
                description = "Regalo secundario: perfume Divine Elixir.",
                isCompleted = false
            ),
            ActivityEntity(
                id = 7,
                order = 7,
                title = "El amarillo te queda bien",
                description = "Regalo principal: collar/colgante (dorado/amarillo).",
                isCompleted = false
            ),
            ActivityEntity(
                id = 8,
                order = 8,
                title = "Acabar de la mejor forma",
                description = "Cena omakase preparada con amor.",
                isCompleted = false
            )
        )
    }
}
