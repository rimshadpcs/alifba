package com.alifba.alifba.presenation.chapters

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.R
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChaptersViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val fireStore = FirebaseFirestore.getInstance()

    private val _chapters = MutableLiveData<List<Chapter>>()
    val chapters: LiveData<List<Chapter>> = _chapters

    val chapterStatuses: StateFlow<Map<String, Boolean>> = dataStoreManager.getChapterStatuses()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun loadChapters(levelId: String) {
        val levelPath = "lessons/level$levelId/chapters"

        fireStore.collection(levelPath)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val chapters = snapshot.documents.mapIndexed { index, doc ->
                        Chapter(
                            id = doc.getLong("id")?.toInt() ?: 0,
                            title = doc.getString("title") ?: "",
                            iconResId = R.drawable.start,
                            isCompleted = false, // Default; will be overridden by DataStore
                            isLocked = index != 0, // Initial lock state
                            isUnlocked = index == 0, // Only first chapter is initially unlocked
                            chapterType = doc.getString("chapterType") ?: ""
                        )
                    }
                    _chapters.value = chapters
                } else {
                    Log.e("ChaptersViewModel", "No chapters found for levelID: $levelId")
                }
            }
            .addOnFailureListener {
                Log.e("ChaptersViewModel", "Error loading chapters for levelID: $levelId", it)
            }
    }
    fun markChapterCompleted(chapterId: Int, nextChapterId: Int?) {
        viewModelScope.launch {
            dataStoreManager.markCompletedChapters(chapterId, nextChapterId)
        }
    }

    fun getNextChapterId(currentChapterId: Int): Int? {
        val chapterList = _chapters.value ?: return null
        val currentIndex = chapterList.indexOfFirst { it.id == currentChapterId }
        return if (currentIndex != -1 && currentIndex < chapterList.size - 1) {
            chapterList[currentIndex + 1].id
        } else {
            null // No next chapter
        }
    }
}
