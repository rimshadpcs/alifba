package com.alifba.alifba.presenation.chapters

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    private val _badgeEarnedEvent = MutableStateFlow<Badge?>(null)
    val badgeEarnedEvent: StateFlow<Badge?> get() = _badgeEarnedEvent

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

            checkAndAwardBadge(chapterId)
        }
    }


    private suspend fun checkAndAwardBadge(chapterId: Int) {
        val userId = dataStoreManager.userId.first()
        if (!userId.isNullOrEmpty()) {
            try {
                val userRef = fireStore.collection("users").document(userId)

                // Fetch current earned badges as an array
                val userDoc = userRef.get().await()
                val earnedBadges = userDoc.get("earned_badges") as? List<String> ?: emptyList()

                // Query badges with the "chapter_completion" criteria
                val badgeQuery = fireStore.collection("badges")
                    .whereEqualTo("criteria.type", "chapter_completion")
                    .get().await()

                for (badgeDoc in badgeQuery.documents) {
                    val badge = badgeDoc.toObject(Badge::class.java) ?: continue

                    if (!earnedBadges.contains(badge.id)) {
                        // Award the badge by appending it to the earned_badges array
                        userRef.update("earned_badges", earnedBadges + badge.id).await()

                        // Trigger badge event to notify the UI
                        _badgeEarnedEvent.value = badge
                        break // Exit loop after awarding the badge to avoid multiple notifications
                    }
                    Log.d("ChaptersViewModel", "Badge earned: ${badge.title}")

                }
            } catch (e: Exception) {
                Log.e("checkAndAwardBadge", "Error awarding badge: ${e.localizedMessage}")
            }
        }
    }

    fun clearBadgeEvent() {
        _badgeEarnedEvent.value = null
    }
    private fun updateStreak() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (!userId.isNullOrEmpty()) {
                try {
                    val userRef = fireStore.collection("users").document(userId)
                    fireStore.runTransaction { transaction ->
                        val snapshot = transaction.get(userRef)

                        val lastActivityDate = snapshot.getString("last_activity_date") ?: ""
                        val currentStreak = snapshot.getLong("day_streak") ?: 0

                        // Get today's date in "yyyy-MM-dd" format
                        val currentDate = getCurrentDate()

                        if (lastActivityDate != currentDate) {
                            // If lastActivityDate is yesterday, continue the streak; otherwise, reset it
                            val newStreak =
                                if (isYesterday(lastActivityDate)) currentStreak + 1 else 1
                            transaction.update(userRef, "last_activity_date", currentDate)
                            transaction.update(userRef, "day_streak", newStreak)
                        }
                    }.await()
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error updating streak: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun isYesterday(lastDate: String): Boolean {
        return false
    }


    fun checkAndMarkChapterCompletion(
        chapterId: String,
        totalLessons: Int,
        levelId: String,
        earnedXP: Int
    ) {
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.userId.first()
                if (!userId.isNullOrEmpty()) {
                    val userRef = fireStore.collection("users").document(userId)

                    fireStore.runTransaction { transaction ->
                        val snapshot = transaction.get(userRef)

                        // Update completed chapters
                        val completedChapters =
                            snapshot.get("chapters_completed") as? MutableList<String>
                                ?: mutableListOf()
                        if (!completedChapters.contains(chapterId)) {
                            completedChapters.add(chapterId)
                            transaction.update(userRef, "chapters_completed", completedChapters)
                        }

                        // Add XP
                        val currentXP = snapshot.getLong("xp") ?: 0
                        transaction.update(userRef, "xp", currentXP + earnedXP)
                    }.await()

                    // Update streak after marking the chapter as completed
                    updateStreak()
                }
            } catch (e: Exception) {
                Log.e("ChaptersViewModel", "Error marking chapter complete: ${e.localizedMessage}")
            }
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
