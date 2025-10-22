package com.example.birthday

import android.app.Application
import com.example.birthday.data.db.AppDatabase
import com.example.birthday.data.repo.CumpleRepository

class CumpleAnaApp : Application() {
    lateinit var repository: CumpleRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = CumpleRepository(
            activityDao = database.activityDao(),
            photoDao = database.photoDao(),
            videoDao = database.videoDao()
        )
    }
}
