package com.alifba.alifba.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alifba.alifba.data.db.DatabaseProvider
import com.alifba.alifba.data.db.LessonCacheEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject

class DownloadLessonWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val chapterId = inputData.getInt("chapter_id", -1)
        val levelId = inputData.getString("level_id")

        return try {
            val firestore = FirebaseFirestore.getInstance()

            val collectionPath = "lessons/$levelId/chapters"
            val querySnapshot = firestore.collection(collectionPath)
                .whereEqualTo("id", chapterId.toLong())
                .get()
                .await()

            if (querySnapshot.documents.isEmpty()) {
                Log.e("DownloadLessonWorker", "No chapter document found for id: $chapterId")
                return Result.failure()
            }



            val chapterDoc = querySnapshot.documents.first()
            Log.d("DownloadLessonWorker", "Full document data: ${chapterDoc.data}")



// In doWork() method, replace the existing URL extraction with:
            val (imageUrl, audioUrl) = findMediaUrls(chapterDoc)
            Log.d("DownloadLessonWorker", "Extracted URLs - Image: $imageUrl, Audio: $audioUrl")
            // Assuming image and speech are direct fields in the chapter document
//            val imageUrl = chapterDoc.getString("image")
//            val audioUrl = chapterDoc.getString("speech")

            Log.d("DownloadLessonWorker", "Extracted URLs - Image: $imageUrl, Audio: $audioUrl")

            val imagePath = imageUrl?.let { downloadFile(it, "chapter_image_$chapterId.jpg") }
            val audioPath = audioUrl?.let { downloadFile(it, "chapter_audio_$chapterId.mp3") }
            Log.d("DownloadLessonWorker", "Downloaded imagePath: $imagePath, audioPath: $audioPath")

            if (imagePath != null && audioPath != null) {
                val db = DatabaseProvider.getInstance(applicationContext)
                db.lessonCacheDao().insert(
                    LessonCacheEntity(
                        chapterId = chapterId,
                        levelId = levelId.toString(),
                        imagePath = imagePath,
                        audioPath = audioPath
                    )
                )
                val inserted = db.lessonCacheDao().getLessonCache(chapterId, levelId.toString())
                Log.d("DownloadLessonWorker", "Inserted entity read-back: $inserted")
            } else {
                Log.e("DownloadLessonWorker", "One or both file paths are null. Insertion skipped.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("DownloadLessonWorker", "Download failed: ${e.stackTraceToString()}")
            Result.failure()
        }
    }


    private suspend fun downloadFile(
        fileUrl: String,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(fileUrl)
            val connection = url.openConnection()
            connection.connect()

            val file = File(applicationContext.cacheDir, fileName)
            file.outputStream().use { output ->
                connection.getInputStream().use { input ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("DownloadLessonWorker", "File download failed: ${e.stackTraceToString()}")
            null
        }
    }
}
fun findMediaUrls(chapterDoc: DocumentSnapshot): Pair<String?, String?> {
    val segments = chapterDoc.get("segments") as? List<Map<String, Any>>

    // First, try to find direct image and speech in segments
    val directImageUrl = segments?.firstNotNullOfOrNull {
        (it["image"] as? String)?.takeIf { img -> img.isNotBlank() }
    }

    val directAudioUrl = segments?.firstNotNullOfOrNull {
        (it["speech"] as? String)?.takeIf { speech -> speech.isNotBlank() }
    }

    // If direct image/speech not found, search in exercises
    val exerciseImageUrl = if (directImageUrl == null) {
        segments?.firstNotNullOfOrNull { segment ->
            val exercise = segment["exercise"] as? Map<String, Any>
            exercise?.get("imageResId") as? String
        }
    } else null

    val exerciseAudioUrl = if (directAudioUrl == null) {
        segments?.firstNotNullOfOrNull { segment ->
            val exercise = segment["exercise"] as? Map<String, Any>
            exercise?.get("speech") as? String
        }
    } else null

    // Prefer direct URLs, fall back to exercise URLs
    return Pair(
        directImageUrl ?: exerciseImageUrl,
        directAudioUrl ?: exerciseAudioUrl
    )
}