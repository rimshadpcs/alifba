package com.alifba.alifba.data.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [LessonCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LessonCacheDatabase : RoomDatabase() {
    abstract fun lessonCacheDao(): LessonCacheDao
}