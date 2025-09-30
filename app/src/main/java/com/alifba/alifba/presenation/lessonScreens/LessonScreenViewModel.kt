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
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
//import com.alifba.alifba.data.models.sampleLessons
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class LessonScreenViewModel  @Inject constructor(
    private val getLessonsUseCase :GetLessonUseCase,
    private val dataStoreManager: DataStoreManager,
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

): ViewModel() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioResId: String? = null
    private var applicationContext: Context? = null // Store the Context
    private val _isAudioCompleted = MutableLiveData<Boolean>(false)
    val isAudioCompleted: LiveData<Boolean> get() = _isAudioCompleted
    private val _lessons = MutableLiveData<List<Lesson>>()
    val lessons: LiveData<List<Lesson>> = _lessons

    private val _loading = MutableLiveData<Boolean>()
    val loading : LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error : LiveData<String?> = _error

    private val _isAudioPlaying = MutableLiveData(false)
    val isAudioPlaying: LiveData<Boolean> get() = _isAudioPlaying

    init {
        mediaPlayer?.setOnCompletionListener {
            _isAudioCompleted.value = true
        }
        mediaPlayer?.setOnErrorListener { _, _, _ ->
            // If there's an error, we may want to allow the user to proceed
            _isAudioCompleted.value = true
            true
        }
    }
    fun initContext(context: Context) {
        applicationContext = context
    }
    private fun configureMediaPlayer(dataSource: () -> Unit) {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
            } ?: run {
                mediaPlayer = MediaPlayer()
            }
            dataSource()
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener { mp ->
                mp.start()
                _isAudioPlaying.value = true // Now playing
            }
            mediaPlayer?.setOnCompletionListener {
                _isAudioCompleted.value = true
                _isAudioPlaying.value = false
            }
        } catch (e: Exception) {
            Log.e("LessonScreenViewModel", "Error configuring MediaPlayer: ", e)
            mediaPlayer?.reset()
            _isAudioPlaying.value = false
        }
    }

    fun startAudio(audioUrl: String) {
        // 1) Mark audio as not completed
        _isAudioCompleted.value = false

        // 2) Proceed with setDataSource, prepareAsync, etc.
        if (applicationContext != null && audioUrl.isNotBlank()) {
            configureMediaPlayer {
                mediaPlayer?.setDataSource(applicationContext!!, Uri.parse(audioUrl))
            }
        }
    }

    fun startLocalAudio(audioResId: Int) {
        if (applicationContext == null) {
            Log.e("LessonScreenViewModel", "Context not set for local audio")
            return
        }
        configureMediaPlayer {
            val afd = applicationContext!!.resources.openRawResourceFd(audioResId)
            mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
        }
    }
    fun stopAudio() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        mediaPlayer?.reset()

        // If we forcibly stop, user might be allowed to proceed:
        _isAudioCompleted.value = true
    }


    fun loadLessons(levelId: String) {
        _loading.value = true
        _error.value = null

        Log.d("LessonScreenViewModel", "Fetching lessons for: $levelId")

        viewModelScope.launch {
            try {
                val lessonList = getLessonsUseCase.invoke(levelId)
                _lessons.value = lessonList
                _loading.value = false

                Log.d("LessonScreenViewModel", "Loaded ${lessonList.size} lessons for $levelId") // ✅ Debug log

            } catch (e: Exception) {
                _error.value = "Failed to load lessons for $levelId."
                _loading.value = false
                Log.e("LessonScreenViewModel", "Error loading lessons: ${e.localizedMessage}")
            }
        }
    }


    fun getLessonContentByID(id: Int): Lesson? {
        val lesson = _lessons.value?.find { it.id == id }
        if (lesson != null) {
            Log.d("LessonScreenViewModel", "Found lesson with ID: $id")
        } else {
            Log.w("LessonScreenViewModel", "Lesson with ID: $id not found in current list.")
        }
        return lesson
    }

    override fun onCleared() {
        mediaPlayer?.release()
        super.onCleared()
    }

//    fun markLessonCompleted(lessonId: Int, nextLessonId: Int?) {
//        viewModelScope.launch {
//            dataStoreManager.markCompletedChapters(lessonId, nextLessonId)
//        }
//    }

    fun incrementQuizzesAttended() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (!userId.isNullOrEmpty()) {
                try {
                    val userRef = fireStore.collection("users").document(userId)
                    fireStore.runTransaction { transaction ->
                        val snapshot = transaction.get(userRef)
                        
                        // Update quizzes attended in profiles array
                        val profiles = snapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                        if (profiles.isNotEmpty()) {
                            val activeProfileIndex = (snapshot.getLong("activeProfileIndex") ?: 0).toInt()
                            if (activeProfileIndex < profiles.size) {
                                val updatedProfiles = profiles.toMutableList()
                                val currentProfile = updatedProfiles[activeProfileIndex].toMutableMap()
                                val currentQuizzesAttended = (currentProfile["quizzesAttended"] as? Long)?.toInt() ?: 0
                                currentProfile["quizzesAttended"] = currentQuizzesAttended + 1
                                updatedProfiles[activeProfileIndex] = currentProfile
                                
                                transaction.update(userRef, "profiles", updatedProfiles)
                                transaction.update(userRef, "lastUpdated", System.currentTimeMillis())
                            }
                        }
                    }.await()
                } catch (e: Exception) {
                    Log.e("LessonScreenViewModel", "Error incrementing quizzes_attended: ${e.localizedMessage}")
                }
            }
        }
    }

}
