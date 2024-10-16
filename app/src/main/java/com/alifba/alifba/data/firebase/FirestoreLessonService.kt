package com.alifba.alifba.data.firebase

import android.util.Log
import com.alifba.alifba.data.models.FillInTheBlanksExercise
import com.alifba.alifba.data.models.Lesson
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.data.models.OptionsForFillInTheBlanks
import com.alifba.alifba.data.models.TextMcqItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FireStoreLessonService(
    private val firestore: FirebaseFirestore
) {
    suspend fun getLessons(): List<Lesson> {
        return try {
            val snapshot = firestore.collection("lessons")
                .document("level1")  // Access the specific level
                .collection("chapters")  // Navigate to the chapters subcollection
                .get()
                .await()

            Log.d("FirestoreLessonService", "Documents fetched from Firestore: ${snapshot.documents.size}")

            val lessons = snapshot.documents.mapNotNull { document ->
                val lessonData = document.data ?: return@mapNotNull null
                val segments = (lessonData["segments"] as? List<Map<String, Any>>)?.mapNotNull { segmentData ->
                    when (segmentData["type"]) {
                        "commonLesson" -> {
                            LessonSegment.CommonLesson(
                                character = segmentData["character"] as? String ?: "",
                                image = segmentData["image"] as? String ?: "",
                                description = segmentData["description"] as? String ?: "",
                                speech = segmentData["speech"] as? String ?: ""
                            )
                        }
                        "fillInTheBlanks" -> {
                            val exercise = segmentData["exercise"] as? Map<String, Any>
                            LessonSegment.FillInTheBlanks(
                                exercise = FillInTheBlanksExercise(
                                    imageResId = exercise?.get("imageResId") as? String ?: "",
                                    speech = exercise?.get("speech") as? String ?: "",
                                    sentenceParts = exercise?.get("sentenceParts") as? List<String> ?: emptyList(),
                                    options = (exercise?.get("options") as? List<Map<String, Any>>)?.map { option ->
                                        OptionsForFillInTheBlanks(option = option["option"] as? String ?: "")
                                    } ?: emptyList(),
                                    correctAnswers = (exercise?.get("correctAnswers") as? List<Number>)?.map { it.toInt() } ?: emptyList()
                                )
                            )
                        }
                        "textMcqLesson" -> {
                            LessonSegment.TextMcqLesson(
                                question = segmentData["question"] as? String ?: "",
                                choices = (segmentData["choices"] as? List<Map<String, Any>>)?.map { choice ->
                                    TextMcqItem(
                                        choice = choice["choice"] as? String ?: "",
                                        answer = choice["answer"] as? Boolean ?: false
                                    )
                                } ?: emptyList(),
                                speech = segmentData["speech"] as? String ?: ""
                            )
                        }
                        else -> null // Unknown type, skip this segment
                    }
                } ?: emptyList()

                Lesson(
                    id = (lessonData["id"] as? Long)?.toInt() ?: 0,
                    title = lessonData["title"] as? String ?: "",
                    segments = segments
                )
            }

            lessons
        } catch (e: Exception) {
            Log.e("FirestoreLessonService", "Error fetching lessons from Firestore", e)
            emptyList()
        }
    }
}
