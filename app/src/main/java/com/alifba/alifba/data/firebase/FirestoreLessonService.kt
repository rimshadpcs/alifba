package com.alifba.alifba.data.firebase

import android.util.Log
import com.alifba.alifba.data.models.FillInTheBlanksExercise
import com.alifba.alifba.data.models.Lesson
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.data.models.OptionsForFillInTheBlanks
import com.alifba.alifba.data.models.PictureMcqItem
import com.alifba.alifba.data.models.TextMcqItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FireStoreLessonService(
    private val firestore: FirebaseFirestore
) {
    suspend fun getLessons(levelId: String): List<Lesson> {
        return try {
            val collectionPath = "lessons/$levelId/chapters"
            Log.d("FirestoreLessonService", "Fetching lessons from: $collectionPath") // ✅ Debug log

            val snapshot = firestore.collection(collectionPath).get().await()

            Log.d("FirestoreLessonService", "Lessons fetched: ${snapshot.documents.size}") // ✅ Debug log

            if (snapshot.isEmpty) {
                Log.e("FirestoreLessonService", "No lessons found for $levelId")
                return emptyList()
            }

            snapshot.documents.mapNotNull { document ->
                val lessonData = document.data ?: return@mapNotNull null

                Lesson(
                    id = (lessonData["id"] as? Long)?.toInt() ?: 0,
                    title = lessonData["title"] as? String ?: "Untitled Lesson",
                    segments = extractSegments(lessonData),
                    chapterType = lessonData["chapterType"] as? String ?: ""
                )
            }

        } catch (e: Exception) {
            Log.e("FirestoreLessonService", "Error fetching lessons: ${e.localizedMessage}")
            emptyList()
        }
    }


    private fun extractSegments(lessonData: Map<String, Any>): List<LessonSegment> {
        return (lessonData["segments"] as? List<Map<String, Any>>)?.mapNotNull { segmentData ->
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
                            sentenceParts = exercise?.get("sentenceParts") as? List<String>
                                ?: emptyList(),
                            options = (exercise?.get("options") as? List<Map<String, Any>>)?.map { option ->
                                OptionsForFillInTheBlanks(
                                    option = option["option"] as? String ?: ""
                                )
                            } ?: emptyList(),
                            correctAnswers = (exercise?.get("correctAnswers") as? List<Number>)?.map { it.toInt() }
                                ?: emptyList()
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

                "pictureMcqLesson" -> {
                    val choices =
                        (segmentData["pictureChoices"] as? List<Map<String, Any>>)?.map { choice ->
                            PictureMcqItem(
                                image = choice["image"].toString(),
                                choice = choice["choice"].toString(),
                                answer = choice["answer"] as? Boolean ?: false
                            )
                        } ?: emptyList()

                    val question = segmentData["question"] as? String ?: ""
                    val image = segmentData["image"].toString()  // Convert to String
                    val correctAnswer = segmentData["correctAnswer"] as? String ?: ""
                    val speech = segmentData["speech"].toString()

                    Log.d(
                        "FirestoreLessonService",
                        "Loaded PictureMcqLesson: image=$image, question=$question"
                    )

                    LessonSegment.PictureMcqLesson(
                        question = question,
                        //image = image,
                        pictureChoices = choices,
                        correctAnswer = correctAnswer,
                        speech = speech
                    )
                }

                "letterTracing" -> {
                    LessonSegment.LetterTracing(
                        letterId = segmentData["letterId"] as? String ?: "",
                        speech = segmentData["speech"] as? String ?: "",
                    )
                }
                "cloudTappingLesson" -> {
                    LessonSegment.CloudTappingLesson(
                        letterId = segmentData["letterId"] as? String ?: "",
                        speech = segmentData["speech"] as? String ?: "",
                        targetLetter = segmentData["targetLetter"] as? String ?: "",
                        nonTargetLetters = (segmentData["nonTargetLetters"] as? List<String>) ?: emptyList()
                    ).also {
                        Log.d("FirestoreLessonService", "Loaded CloudTappingLesson: targetLetter=${it.targetLetter}, nonTargetLetters=${it.nonTargetLetters}")
                    }
                }



                else -> null
            }
        } ?: emptyList()

    }
}
