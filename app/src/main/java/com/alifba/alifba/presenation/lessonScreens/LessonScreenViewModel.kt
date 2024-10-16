package com.alifba.alifba.presenation.lessonScreens

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.data.models.Lesson
import com.alifba.alifba.presenation.lessonScreens.usecases.GetLessonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
//import com.alifba.alifba.data.models.sampleLessons
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class LessonScreenViewModel  @Inject constructor(
    private val getLessonsUseCase :GetLessonUseCase
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
            if (player.isPlaying) {
                player.stop()
                player.reset()
            }

            applicationContext?.let { context ->
                try {
                    val audioUri = Uri.parse(audioUrl)  // Use URL or Firebase path directly
                    player.setDataSource(context, audioUri)
                    player.prepareAsync() // Prepare asynchronously to avoid blocking the main thread
                    player.setOnPreparedListener {
                        player.start()
                    }
                    currentAudioResId = audioUrl
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

//    fun getLessonContentById(id: Int): Lesson? {
//        // Fetch the lesson content by ID from the static list
//        return sampleLessons.find { it.id == id }
//    }

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


    fun getLessonContentByID(id:Int):Lesson?{
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
}
