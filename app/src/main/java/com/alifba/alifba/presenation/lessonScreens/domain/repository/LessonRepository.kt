package com.alifba.alifba.presenation.lessonScreens.domain.repository

import com.alifba.alifba.data.models.Lesson

interface LessonRepository{
    suspend fun getLessons(): List<Lesson>
}
