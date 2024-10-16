package com.alifba.alifba.presenation.lessonScreens.usecases

import com.alifba.alifba.data.models.Lesson
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonRepository

class GetLessonUseCase(
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(): List<Lesson>{
        return lessonRepository.getLessons()
    }

}