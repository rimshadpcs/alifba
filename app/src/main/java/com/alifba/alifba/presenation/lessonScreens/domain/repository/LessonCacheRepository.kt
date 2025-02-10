package com.alifba.alifba.presenation.lessonScreens.domain.repository

import com.alifba.alifba.data.db.LessonCacheEntity
interface LessonCacheRepository {
    suspend fun cacheLesson(
        chapterId: Int,
        levelId: String,
        imagePath: String?,
        audioPath: String?
    )
    suspend fun getLessonCache(
        chapterId: Int,
        levelId: String
    ): LessonCacheEntity?
}
