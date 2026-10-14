package com.alifba.alifba.utils

import android.content.Context
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.security.MessageDigest

/**
 * Real offline support for a lesson: every image and audio file a lesson uses is fetched when the
 * learner taps "Download lesson", so no lesson screen has to wait on the network afterwards.
 * Images go into Coil's disk cache (see AlifbaApp.newImageLoader, which ignores Firebase's
 * max-age=0 headers) and audio into plain files that LessonScreenViewModel.startAudio plays from.
 */
object LessonMediaCache {
    private const val TAG = "LessonMediaCache"
    private val audioExtensions = listOf(".mp3", ".m4a", ".aac", ".wav", ".ogg")
    private val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".gif")

    private fun audioDir(context: Context) = File(context.cacheDir, "lesson_audio").apply { mkdirs() }

    private fun hash(url: String): String =
        MessageDigest.getInstance("SHA-1").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }

    /** Local file for [url]'s audio if it has been downloaded, else null */
    fun cachedAudio(context: Context, url: String): File? =
        File(audioDir(context), hash(url)).takeIf { it.exists() && it.length() > 0 }

    private fun pathOf(url: String) = url.substringBefore('?').lowercase()

    /** All media URLs anywhere in the chapter's segments (segments, exercises, picture choices…) */
    fun collectMedia(chapterDoc: DocumentSnapshot): Pair<Set<String>, Set<String>> {
        val images = linkedSetOf<String>()
        val audio = linkedSetOf<String>()
        fun walk(value: Any?) {
            when (value) {
                is String -> if (value.startsWith("http")) {
                    val path = pathOf(value)
                    when {
                        audioExtensions.any { path.endsWith(it) } -> audio += value
                        imageExtensions.any { path.endsWith(it) } -> images += value
                    }
                }
                is Map<*, *> -> value.values.forEach(::walk)
                is List<*> -> value.forEach(::walk)
            }
        }
        walk(chapterDoc.get("segments"))
        return images to audio
    }

    /** Downloads every image and audio file of the lesson; failures of single files are ignored */
    suspend fun prefetch(context: Context, chapterDoc: DocumentSnapshot) = withContext(Dispatchers.IO) {
        val (images, audio) = collectMedia(chapterDoc)
        Log.d(TAG, "Prefetching ${images.size} images and ${audio.size} audio files")
        val loader = context.imageLoader
        images.forEach { url ->
            runCatching { loader.execute(ImageRequest.Builder(context).data(url).build()) }
        }
        audio.forEach { url ->
            if (cachedAudio(context, url) != null) return@forEach
            runCatching {
                val target = File(audioDir(context), hash(url))
                val tmp = File(target.path + ".part")
                URL(url).openStream().use { input -> tmp.outputStream().use { input.copyTo(it) } }
                tmp.renameTo(target)
            }.onFailure { Log.w(TAG, "Audio prefetch failed for $url", it) }
        }
    }
}
