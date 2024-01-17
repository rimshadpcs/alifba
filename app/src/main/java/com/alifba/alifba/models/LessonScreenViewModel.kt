package com.alifba.alifba.models

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import java.io.IOException

class LessonScreenViewModel : ViewModel() {
    private val mediaPlayer: MediaPlayer? by lazy { MediaPlayer() }
    private var currentAudioResId: Int? = null
    private var applicationContext: Context? = null // Store the Context

    init {
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.setOnCompletionListener {
                currentAudioResId = null // Reset the current audio ID on completion
            }
        }
    }
    fun initContext(context: Context) {
        applicationContext = context
    }
    fun startAudio(audioResId: Int) {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
                player.reset()
            }

            applicationContext?.let { context ->
                try {
                    val audioUri = Uri.parse("android.resource://${context.packageName}/$audioResId")
                    player.setDataSource(context, audioUri)
                    player.prepare()
                    player.start()
                    currentAudioResId = audioResId
                } catch (e: IOException) {
                    e.printStackTrace()
                    // Handle the exception as needed
                }
            }
        }
    }
    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        currentAudioResId = null
    }

    fun getLessonContentById(id: Int): Lesson? {
        // Fetch the lesson content by ID from the static list
        return sampleLessons.find { it.id == id }
    }
    override fun onCleared() {
        mediaPlayer?.release()
        super.onCleared()
    }
}
