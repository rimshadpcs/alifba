package com.alifba.alifba.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File
import java.net.URL

class DownloadLessonWorker(
    context: Context, workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val chapterId = inputData.getInt("chapter_id", -1)
        if (chapterId == -1) return Result.failure()

        // Fetch chapter data from Firestore and download resources
        val firestore = FirebaseFirestore.getInstance()
        val chapter = firestore.collection("lessons").document("level1")
            .collection("chapters").document(chapterId.toString())
            .get().await()

        val imageUrl = chapter.getString("imageUrl")
        val audioUrl = chapter.getString("audioUrl")

        // Download image and audio
        imageUrl?.let { downloadFile(it, "chapter_image_$chapterId.jpg") }
        audioUrl?.let { downloadFile(it, "chapter_audio_$chapterId.mp3") }

        return Result.success()
    }

    private suspend fun downloadFile(fileUrl: String, fileName: String) {
        val url = URL(fileUrl)
        val connection = url.openConnection()
        connection.connect()

        val file = File(applicationContext.cacheDir, fileName)
        file.outputStream().use { output ->
            connection.getInputStream().use { input ->
                input.copyTo(output)
            }
        }
    }
}
