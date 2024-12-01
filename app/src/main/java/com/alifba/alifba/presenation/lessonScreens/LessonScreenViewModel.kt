package com.alifba.alifba.presenation.lessonScreens

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.data.models.Lesson
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
//import com.alifba.alifba.features.authentication.dataStore
import com.alifba.alifba.presenation.lessonScreens.usecases.GetLessonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
//import com.alifba.alifba.data.models.sampleLessons
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class LessonScreenViewModel  @Inject constructor(
    private val getLessonsUseCase :GetLessonUseCase,
    private val dataStoreManager: DataStoreManager,
): ViewModel() {
    private val mediaPlayer: MediaPlayer? by lazy { MediaPlayer() }
    private var currentAudioResId: String? = null
    private var applicationContext: Context? = null // Store the Context

    private val _lessons = MutableLiveData<List<Lesson>>()
    val lessons: LiveData<List<Lesson>> = _lessons

    private val _loading = MutableLiveData<Boolean>()
    val loading : LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error : LiveData<String?> = _error


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
    fun startAudio(audioUrl: String) {
        mediaPlayer?.let { player ->
            try {
                // Stop and reset only if the player is already in use
                if (player.isPlaying) {
                    player.stop()
                    player.reset()
                }

                // Verify URL and context before attempting to set the data source
                if (audioUrl.isBlank() || applicationContext == null) {
                    Log.e("LessonScreenViewModel", "Invalid audio URL or context not set.")
                    return
                }

                val audioUri = Uri.parse(audioUrl)
                player.setDataSource(applicationContext!!, audioUri)

                // Asynchronously prepare the player, so it doesn't block the main thread
                player.prepareAsync()
                player.setOnPreparedListener {
                    player.start()
                    Log.d("LessonScreenViewModel", "Streaming audio started: $audioUrl")
                }
                currentAudioResId = audioUrl

            } catch (e: IllegalStateException) {
                Log.e("LessonScreenViewModel", "MediaPlayer is in an invalid state", e)
                player.reset()
            } catch (e: IOException) {
                Log.e("LessonScreenViewModel", "Error setting data source for audio URL", e)
            }
        }
    }

    fun startLocalAudio(audioResId: Int) {
        mediaPlayer?.let { player ->
            try {
                // Stop and reset only if the player is already in use
                if (player.isPlaying) {
                    player.stop()
                    player.reset()
                }

                applicationContext?.let { context ->
                    val afd = context.resources.openRawResourceFd(audioResId)
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()

                    player.prepare()
                    player.start()
                    Log.d("LessonScreenViewModel", "Local audio started: $audioResId")
                }

            } catch (e: IllegalStateException) {
                Log.e("LessonScreenViewModel", "Error in MediaPlayer state for local audio", e)
                player.reset()
            } catch (e: IOException) {
                Log.e("LessonScreenViewModel", "Error setting data source for local audio", e)
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        currentAudioResId = null
    }

    fun loadLessons() {
        _loading.value = true
        _error.value = null  // Reset error before a new attempt
        Log.d("LessonScreenViewModel", "Loading lessons from Firestore...")

        viewModelScope.launch {
            try {
                val lessonList = getLessonsUseCase()
                _lessons.value = lessonList
                _loading.value = false
                Log.d("LessonScreenViewModel", "Successfully loaded lessons: ${lessonList.size} lessons.")
            } catch (e: Exception) {
                _error.value = "Failed to load lessons."
                _loading.value = false
                Log.e("LessonScreenViewModel", "Error loading lessons from Firestore.", e)
            }
        }
    }

    fun getLessonContentByID(id: Int): Lesson? {
        val lesson = _lessons.value?.find { it.id == id }
        if (lesson != null) {
            Log.d("LessonScreenViewModel", "Found lesson with ID: $id")
        } else {
            Log.w("LessonScreenViewModel", "Lesson with ID: $id not found.")
        }
        return lesson
    }

    override fun onCleared() {
        mediaPlayer?.release()
        super.onCleared()
    }

    fun markLessonCompleted(lessonId: Int, nextLessonId: Int?) {
        viewModelScope.launch {
            dataStoreManager.markCompletedChapters(lessonId, nextLessonId)
        }
    }



}
