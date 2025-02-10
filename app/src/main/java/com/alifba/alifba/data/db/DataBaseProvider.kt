package com.alifba.alifba.data.db


import android.content.Context
import androidx.room.Room

import android.util.Log

object DatabaseProvider {

    @Volatile
    private var instance: LessonCacheDatabase? = null

    fun getInstance(context: Context): LessonCacheDatabase {
        val appContext = context.applicationContext
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(appContext).also {
                instance = it
                Log.d("DatabaseProvider", "Created DB instance: ${it.hashCode()}")
            }
        }
    }

    private fun buildDatabase(context: Context): LessonCacheDatabase {
        return Room.databaseBuilder(
            context,
            LessonCacheDatabase::class.java,
            "lesson_cache_database"
        ).build().also {
            Log.d("DatabaseProvider", "Built DB instance: ${it.hashCode()}")
        }
    }
}
