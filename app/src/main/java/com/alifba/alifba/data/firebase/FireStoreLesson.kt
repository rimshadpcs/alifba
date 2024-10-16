package com.alifba.alifba.data.firebase

import com.alifba.alifba.data.models.FillInTheBlanksExercise
import com.alifba.alifba.data.models.Lesson
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.data.models.OptionsForFillInTheBlanks
import com.alifba.alifba.data.models.TextMcqItem

data class FirestoreLesson(
    val id: Int = 0,
    val title: String = "",
    val segments: List<Map<String, Any>> = emptyList() // Firestore segments as raw data
) {
    fun toDomainModel(): Lesson {
        return Lesson(
            id = id,
            title = title,
            segments = segments.mapNotNull { segment ->
                when (segment["type"]) {
                    "commonLesson" -> {
                        LessonSegment.CommonLesson(
                            character = segment["character"] as String,
                            image = segment["image"] as String,
                            description = segment["description"] as String,
                            speech = segment["speech"] as String
                        )
                    }
                    "fillInTheBlanks" -> {
                        val exercise = segment["exercise"] as Map<String, Any>
                        LessonSegment.FillInTheBlanks(
                            exercise = FillInTheBlanksExercise(
                                imageResId = exercise["imageResId"] as String,
                                speech = exercise["speech"] as String,
                                sentenceParts = exercise["sentenceParts"] as List<String>,
                                options = (exercise["options"] as List<Map<String, Any>>).map {
                                    val optionText = it["option"] as String
                                    OptionsForFillInTheBlanks(option = optionText) // Map to custom data class
                                },
                                correctAnswers = (exercise["correctAnswers"] as? List<Number>)?.map { it.toInt() } ?: emptyList()
                            )
                        )
                    }
                    "textMcqLesson" -> {
                        LessonSegment.TextMcqLesson(
                            question = segment["question"] as String,
                            choices = (segment["choices"] as List<Map<String, Any>>).map {
                                TextMcqItem(
                                    choice = it["choice"] as String,
                                    answer = it["answer"] as Boolean
                                )

                            },
                            speech = segment["speech"] as String
                        )
                    }
                    else -> null // Handle other types or log unknown segments
                }
            }
        )
    }
}
