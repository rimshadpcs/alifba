package com.alifba.alifba.data.db

import androidx.room.Entity

@Entity(tableName = "lesson_cache", primaryKeys = ["chapterId", "levelId"])
data class LessonCacheEntity(
    val chapterId: Int,
    val levelId: String,
    val imagePath: String,
    val audioPath: String
)

