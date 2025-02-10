package com.alifba.alifba.data.repository

import com.alifba.alifba.data.db.LessonCacheDao
import com.alifba.alifba.data.db.LessonCacheEntity
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonCacheRepository
import javax.inject.Inject

class LessonCacheRepositoryImpl @Inject constructor(
    private val lessonCacheDao: LessonCacheDao
) : LessonCacheRepository {
    override suspend fun cacheLesson(
        chapterId: Int,
        levelId: String,
        imagePath: String?,
        audioPath: String?
    ) {
        lessonCacheDao.insert(
            LessonCacheEntity(
                chapterId = chapterId,
                levelId = levelId,
                imagePath = imagePath ?: "",
                audioPath = audioPath ?: ""
            )
        )
    }

    override suspend fun getLessonCache(
        chapterId: Int,
        levelId: String
    ): LessonCacheEntity? {
        return lessonCacheDao.getLessonCache(chapterId, levelId)
    }
}