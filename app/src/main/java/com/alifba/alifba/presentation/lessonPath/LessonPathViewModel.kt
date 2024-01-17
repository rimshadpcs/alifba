package com.alifba.alifba.presentation.lessonPath

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alifba.alifba.R
import com.alifba.alifba.presentation.lessonPath.models.LessonPathItem

class LessonPathViewModel : ViewModel() {

    private val _introductionLessons = MutableLiveData(listOf(
        LessonPathItem(id = 101, title = "Our Allah", isCompleted = true, isLocked = false, iconResId = R.drawable.start, isUnlocked = true),
        LessonPathItem(id = 102, title = "Our Prophet", isCompleted = false, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 103, title = "Adab", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 104, title = "Pillars", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 105, title = "Imaan", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 101, title = "Our Allah", isCompleted = true, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 102, title = "Our Prophet", isCompleted = false, isLocked = false,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 103, title = "Adab", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 104, title = "Pillars", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false),
        LessonPathItem(id = 105, title = "Imaan", isCompleted = false, isLocked = true,iconResId = R.drawable.padlock, isUnlocked = false)
    ))

    val introductionLessons: LiveData<List<LessonPathItem>> = _introductionLessons

    fun completeLesson(lessonId: Int) {
        val updatedLessons = _introductionLessons.value?.mapIndexed { index, lesson ->
            when {
                lesson.id == lessonId -> lesson.copy(
                    isCompleted = true,
                    iconResId = R.drawable.balloon,
                    isLocked = false
                )

                // Check if the current lesson is the one immediately after the completed lesson
                _introductionLessons.value?.getOrNull(index - 1)?.isCompleted == true && lesson.id != lessonId && !lesson.isCompleted -> lesson.copy(
                    isLocked = false,
                    iconResId = R.drawable.start,
                    isUnlocked = true
                )

                else -> lesson
            }
        }

        // Update the LiveData with the new list
        _introductionLessons.value = updatedLessons
    }

}

