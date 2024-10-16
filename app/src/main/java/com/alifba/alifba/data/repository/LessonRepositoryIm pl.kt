package com.alifba.alifba.data.repository

import com.alifba.alifba.data.firebase.FireStoreLessonService
import com.alifba.alifba.data.models.Lesson
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonRepository

class LessonRepositoryImpl(private val fireStoreLessonService: FireStoreLessonService) : LessonRepository{
    override suspend fun getLessons(): List<Lesson> {
        return fireStoreLessonService.getLessons()
    }
}