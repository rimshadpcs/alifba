package com.alifba.alifba.presenation.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.presenation.home.model.LevelItem
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonCacheRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
open class HomeViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    val lessonCacheRepository: LessonCacheRepository
) : ViewModel() {
    
    private val fireStore = FirebaseFirestore.getInstance()
    
    // Keep level list for backward compatibility (if needed elsewhere)
    val levelItemList = listOf(
        LevelItem("Level 1", R.drawable.levelone, "level1"),
        LevelItem("Level 2",R.drawable.leveltwo,"level2"),
        LevelItem("Level 3", R.drawable.levelthree, "level3"),
        LevelItem("Level 4",R.drawable.levelfour,"level4"),
        LevelItem("Level 5",R.drawable.levelfive,"level5"),
        LevelItem("Level 6", R.drawable.levelseven, "level6"),
        LevelItem("Level 7",R.drawable.levelsix,"level7"),
        LevelItem("Level 8",R.drawable.leveleight,"level8"),
        LevelItem("Level 9", R.drawable.levelnine, "level9"),
        LevelItem("Level 10",R.drawable.levelten,"level10"),
    )
    
    // Chapter loading functionality
    private val _chapters = MutableLiveData<List<Chapter>>()
    val chapters: LiveData<List<Chapter>> = _chapters
    
    val chapterStatuses: StateFlow<Map<String, Boolean>> = dataStoreManager.getChapterStatuses()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    
    private val _badgeEarnedEvent = MutableStateFlow<Badge?>(null)
    val badgeEarnedEvent: StateFlow<Badge?> get() = _badgeEarnedEvent
    
    // Auto-load level 1 chapters on initialization
    init {
        listenForChapterUpdates()
    }
    
    private fun loadLevel1Chapters() {
        listenForChapterUpdates("level1")
    }
    
    fun listenForChapterUpdates(levelId: String = "level1") {
        val levelPath = "lessons/$levelId/chapters"
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (userId.isNullOrEmpty()) {
                Log.e("HomeViewModel", "User ID is null or empty")
                return@launch
            }

            val userDocRef = fireStore.collection("users").document(userId)
            userDocRef.addSnapshotListener { userSnapshot, e ->
                if (e != null) {
                    Log.w("HomeViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (userSnapshot != null && userSnapshot.exists()) {
                    Log.d("HomeViewModel", "Snapshot listener triggered")
                    val profiles = userSnapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                    val activeProfileIndex = (userSnapshot.getLong("activeProfileIndex") ?: 0).toInt()
                    val completedChapters = if (profiles.isNotEmpty() && activeProfileIndex < profiles.size) {
                        profiles[activeProfileIndex]["chaptersCompleted"] as? List<String> ?: emptyList()
                    } else {
                        emptyList()
                    }
                    Log.d("HomeViewModel", "Updated completedChapters: $completedChapters")
                    loadChapters(levelId, completedChapters)
                } else {
                    Log.d("HomeViewModel", "Current data: null")
                }
            }
        }
    }

    private fun loadChapters(levelId: String, completedChapters: List<String>) {
        val levelPath = "lessons/$levelId/chapters"
        viewModelScope.launch {
            try {
                val snapshot = fireStore.collection(levelPath).get().await()
                Log.d("HomeViewModel", "Fetched ${snapshot.documents.size} chapters for level $levelId")

                val chaptersRaw = snapshot.documents.map { doc ->
                    val numericId = doc.getLong("id")?.toInt() ?: 0
                    Chapter(
                        id = numericId,
                        title = doc.getString("title") ?: "Untitled",
                        iconResId = when (doc.getString("chapterType")) {
                            "Lessson" -> R.drawable.book
                            "Story" -> R.drawable.story
                            "Alphabet" -> R.drawable.alphab
                            else -> R.drawable.start
                        },
                        isCompleted = completedChapters.contains(numericId.toString()),
                        isLocked = true,
                        isUnlocked = false,
                        chapterType = doc.getString("chapterType") ?: ""
                    )
                }.sortedBy { it.id }

                val updatedChapters = updateChapterStates(completedChapters, chaptersRaw)
                _chapters.value = updatedChapters
                Log.d("HomeViewModel", "_chapters LiveData updated")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching chapters: ${e.localizedMessage}")
            }
        }
    }
    
    private fun updateChapterStates(
        completedChapters: List<String>,
        chapters: List<Chapter>
    ): List<Chapter> {
        return chapters.mapIndexed { index, chapter ->
            val isCompleted = completedChapters.contains(chapter.id.toString())
            val isLocked = when {
                index == 0 -> false
                completedChapters.contains(chapters[index - 1].id.toString()) -> false
                else -> true
            }
            val iconResId = when {
                isCompleted -> R.drawable.tick
                isLocked -> R.drawable.padlock
                else -> {
                    when (chapter.chapterType) {
                        "Story" -> R.drawable.book
                        "Alphabet" -> R.drawable.alphab
                        else -> R.drawable.start
                    }
                }
            }
            chapter.copy(
                isCompleted = isCompleted,
                isLocked = isLocked,
                isUnlocked = !isLocked,
                iconResId = iconResId
            )
        }
    }
    
    fun clearBadgeEvent() {
        _badgeEarnedEvent.value = null
    }
}