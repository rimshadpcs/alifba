package com.alifba.alifba.presenation.chapters

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonCacheRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    val lessonCacheRepository: LessonCacheRepository
) : ViewModel() {

    private val fireStore = FirebaseFirestore.getInstance()

    private val _chapters = MutableLiveData<List<Chapter>>()
    val chapters: LiveData<List<Chapter>> = _chapters

    val chapterStatuses: StateFlow<Map<String, Boolean>> = dataStoreManager.getChapterStatuses()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _badgeEarnedEvent = MutableStateFlow<List<Badge>>(emptyList())
    val badgeEarnedEvent: StateFlow<List<Badge>> get() = _badgeEarnedEvent

    private val _levelSummary = MutableStateFlow<LevelSummary?>(null)
    val levelSummary: StateFlow<LevelSummary?> = _levelSummary.asStateFlow()

    fun loadChapters(levelId: String) {
        val levelPath = "lessons/$levelId/chapters"  // Correct Firestore path

        Log.d("ChaptersViewModel", "Fetching chapters for level: $levelId from $levelPath")
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.userId.first()
                if (userId.isNullOrEmpty()) {
                    Log.e("ChaptersViewModel", "User ID is null or empty")
                    return@launch
                }

                // Fetch chapters from Firestore
                val snapshot = fireStore.collection(levelPath).get().await()
                Log.d("ChaptersViewModel", "Fetched ${snapshot.documents.size} chapters")

                // Fetch completed chapters from user data - from profiles array
                val userDoc = fireStore.collection("users").document(userId).get().await()
                val profiles = userDoc.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                val activeProfileIndex = (userDoc.getLong("activeProfileIndex") ?: 0).toInt()
                val completedChapters = if (profiles.isNotEmpty() && activeProfileIndex < profiles.size) {
                    profiles[activeProfileIndex]["chaptersCompleted"] as? List<String> ?: emptyList()
                } else {
                    emptyList()
                }

                if (snapshot.isEmpty) {
                    Log.e("ChaptersViewModel", "No chapters found in Firestore for $levelId")
                }

                val chaptersRaw = snapshot.documents.map { doc ->
                    val numericId = doc.getLong("id")?.toInt() ?: 0
                    Chapter(
                        id = numericId,
                        title = doc.getString("title") ?: "Untitled",
                        iconResId = when (doc.getString("chapterType")) {
                            "Lessson" -> R.drawable.book  // Note: correct this if needed
                            "Story" -> R.drawable.story
                            "Alphabet" -> R.drawable.alphab
                            else -> R.drawable.start
                        },
                        // Use the numeric id (converted to string) for consistency
                        isCompleted = completedChapters.contains(numericId.toString()),
                        isLocked = true,
                        isUnlocked = false,
                        chapterType = doc.getString("chapterType") ?: ""
                    )
                }.sortedBy { it.id }
                val updatedChapters = updateChapterStates(completedChapters, chaptersRaw)
                _chapters.value = updatedChapters
            } catch (e: Exception) {
                Log.e("ChaptersViewModel", "Error fetching chapters: ${e.localizedMessage}")
            }
        }
    }


    fun clearBadgeEvent() {
        _badgeEarnedEvent.value = emptyList()
    }

    private fun updateStreak() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (userId.isNullOrEmpty()) return@launch
            try {
                val userRef = fireStore.collection("users").document(userId)
                fireStore.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val lastActivityDate = snapshot.getString("last_activity_date") ?: ""
                    val currentDate = getCurrentDate()
                    
                    // Get profiles array and update day streak for active profile
                    val profiles = snapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                    if (profiles.isNotEmpty()) {
                        val activeProfileIndex = (snapshot.getLong("activeProfileIndex") ?: 0).toInt()
                        if (activeProfileIndex < profiles.size) {
                            val updatedProfiles = profiles.toMutableList()
                            val currentProfile = updatedProfiles[activeProfileIndex].toMutableMap()
                            val currentStreak = (currentProfile["dayStreak"] as? Long)?.toInt() ?: 0
                            
                            Log.d("StreakDebug", "Last activity: '$lastActivityDate', Current date: '$currentDate', Current streak: $currentStreak")
                            Log.d("StreakDebug", "FORCING dayStreak update for testing...")
                            
                            // TEMPORARY: Always update dayStreak to test
                            val newStreak = currentStreak + 1
                            currentProfile["dayStreak"] = newStreak
                            updatedProfiles[activeProfileIndex] = currentProfile
                            
                            transaction.update(userRef, "profiles", updatedProfiles)
                            transaction.update(userRef, "last_activity_date", currentDate)
                            transaction.update(userRef, "lastUpdated", System.currentTimeMillis())
                            
                            Log.d("StreakDebug", "FORCED streak update to: $newStreak")
                        }
                    }
                }.await()
                checkAndAwardBadges(userId)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating streak: ${e.localizedMessage}")
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun isYesterday(lastDate: String): Boolean {
        if (lastDate.isEmpty()) return false
        
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastActivityDate = dateFormat.parse(lastDate) ?: return false
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }.time
            
            val yesterdayStr = dateFormat.format(yesterday)
            return lastDate == yesterdayStr
        } catch (e: Exception) {
            Log.e("ChaptersViewModel", "Error parsing date for streak calculation: ${e.localizedMessage}")
            return false
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

    fun checkAndMarkChapterCompletion(
        chapterId: String,
        levelId: String,
        earnedXP: Int,
        chapterType: String
    ) {
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.userId.first()
                if (userId.isNullOrEmpty()) return@launch

                android.util.Log.d("ChaptersViewModel", "Fast chapter completion update for: $chapterId")
                val userRef = fireStore.collection("users").document(userId)
                
                // STREAMLINED: Single transaction for immediate update
                fireStore.runTransaction { transaction ->
                    val userSnapshot = transaction.get(userRef)
                    
                    // Get current profiles array
                    val profiles = userSnapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                    if (profiles.isEmpty()) return@runTransaction
                    
                    val activeProfileIndex = (userSnapshot.getLong("activeProfileIndex") ?: 0).toInt()
                    if (activeProfileIndex >= profiles.size) return@runTransaction
                    
                    val updatedProfiles = profiles.toMutableList()
                    val currentProfile = updatedProfiles[activeProfileIndex].toMutableMap()
                    
                    // Update completion lists
                    val allChaptersCompleted = (currentProfile["chaptersCompleted"] as? MutableList<String>) ?: mutableListOf()
                    if (!allChaptersCompleted.contains(chapterId)) {
                        allChaptersCompleted.add(chapterId)
                        currentProfile["chaptersCompleted"] = allChaptersCompleted
                    }
                    
                    // Update XP
                    val currentXP = (currentProfile["xp"] as? Long)?.toInt() ?: 0
                    currentProfile["xp"] = currentXP + earnedXP
                    
                    // Update category-specific lists
                    when (chapterType) {
                        "Lesson" -> {
                            val lessonsCompleted = (currentProfile["lessonsCompleted"] as? MutableList<String>) ?: mutableListOf()
                            if (!lessonsCompleted.contains(chapterId)) {
                                lessonsCompleted.add(chapterId)
                                currentProfile["lessonsCompleted"] = lessonsCompleted
                            }
                        }
                        "Story" -> {
                            val storiesCompleted = (currentProfile["storiesCompleted"] as? MutableList<String>) ?: mutableListOf()
                            if (!storiesCompleted.contains(chapterId)) {
                                storiesCompleted.add(chapterId)
                                currentProfile["storiesCompleted"] = storiesCompleted
                            }
                        }
                        "Alphabet" -> {
                            val activitiesCompleted = (currentProfile["activitiesCompleted"] as? MutableList<String>) ?: mutableListOf()
                            if (!activitiesCompleted.contains(chapterId)) {
                                activitiesCompleted.add(chapterId)
                                currentProfile["activitiesCompleted"] = activitiesCompleted
                            }
                        }
                    }
                    
                    updatedProfiles[activeProfileIndex] = currentProfile
                    
                    // Single transaction update
                    transaction.update(userRef, "profiles", updatedProfiles)
                    transaction.update(userRef, "lastUpdated", System.currentTimeMillis())
                }.await()
                
                android.util.Log.d("ChaptersViewModel", "Fast chapter completion completed")
                
                // Do heavy operations AFTER the main update (background)
                checkAndAwardBadges(userId)
                
            } catch (e: Exception) {
                Log.e("ChaptersViewModel", "Error in fast chapter completion: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun checkAndAwardBadges(userId: String) {
        try {
            val userRef = fireStore.collection("users").document(userId)
            val userDoc = userRef.get().await()
            
            // Get stats from profiles array
            val profiles = userDoc.get("profiles") as? List<Map<String, Any>> ?: emptyList()
            if (profiles.isEmpty()) return
            
            val activeProfileIndex = (userDoc.getLong("activeProfileIndex") ?: 0).toInt()
            if (activeProfileIndex >= profiles.size) return
            
            val currentProfile = profiles[activeProfileIndex]
            val earnedBadges = currentProfile["earnedBadges"] as? List<String> ?: emptyList()
            
            val badgesSnapshot = fireStore.collection("badges").get().await()
            val badges = badgesSnapshot.documents.mapNotNull { it.toObject(Badge::class.java) }
            
            val quizzesAttended = (currentProfile["quizzesAttended"] as? Long) ?: 0
            val storiesCount = (currentProfile["storiesCompleted"] as? List<*>)?.size ?: 0
            val chaptersCount = (currentProfile["chaptersCompleted"] as? List<*>)?.size ?: 0
            val levelsCount = (currentProfile["levelsCompleted"] as? List<*>)?.size ?: 0
            val dayStreak = (currentProfile["dayStreak"] as? Long) ?: 0
            val xp = (currentProfile["xp"] as? Long) ?: 0

            val newlyEarnedBadges = mutableListOf<Badge>()
            val badgeIdsToAdd = mutableListOf<String>()

            badges.forEach { badge ->
                if (!earnedBadges.contains(badge.id)) {
                    val shouldAward = when (badge.criteria.type) {
                        "quiz_completion" -> quizzesAttended >= (badge.criteria.count ?: 0)
                        "story_completion" -> storiesCount >= (badge.criteria.count ?: 0)
                        "chapter_completion" -> chaptersCount >= (badge.criteria.count ?: 0)
                        "level_completion" -> levelsCount >= (badge.criteria.count ?: 0)
                        "streak" -> dayStreak >= (badge.criteria.count ?: 0)
                        "xp_earned" -> xp >= (badge.criteria.count ?: 0)
                        else -> false
                    }
                    if (shouldAward) {
                        newlyEarnedBadges.add(badge)
                        badgeIdsToAdd.add(badge.id)
                        Log.d("BadgeCheck", "Badge earned: ${badge.title}")
                    }
                }
            }

            // Update all newly earned badges at once
            if (newlyEarnedBadges.isNotEmpty()) {
                val updatedProfiles = profiles.toMutableList()
                val updatedProfile = updatedProfiles[activeProfileIndex].toMutableMap()
                val updatedEarnedBadges = (updatedProfile["earnedBadges"] as? MutableList<String>) ?: mutableListOf()
                updatedEarnedBadges.addAll(badgeIdsToAdd)
                updatedProfile["earnedBadges"] = updatedEarnedBadges
                updatedProfiles[activeProfileIndex] = updatedProfile

                userRef.update(
                    mapOf(
                        "profiles" to updatedProfiles,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                ).await()

                // Set all newly earned badges at once
                _badgeEarnedEvent.value = newlyEarnedBadges
                Log.d("BadgeCheck", "Total badges earned: ${newlyEarnedBadges.size}")
            }
        } catch (e: Exception) {
            Log.e("BadgeCheck", "Error checking badges: ${e.localizedMessage}")
        }
    }

    fun getLevelSummary(levelId: String) {
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.userId.first()
                if (userId.isNullOrEmpty()) return@launch
                val levelPath = "lessons/$levelId"
                val levelDoc = fireStore.document(levelPath).get().await()
                val description = levelDoc.getString("description") ?: "No description available"
                val chaptersSnapshot = fireStore.collection("$levelPath/chapters").get().await()
                if (!chaptersSnapshot.isEmpty) {
                    var totalLessons = 0
                    var totalStories = 0
                    var totalQuizzes = 0
                    var totalActivities = 0
                    chaptersSnapshot.documents.forEach { doc ->
                        when (doc.getString("chapterType")) {
                            "Lesson" -> totalLessons++
                            "Story" -> totalStories++
                            "Quiz" -> totalQuizzes++
                            "Alphabet" -> totalActivities++
                        }
                    }
                    Log.d("LevelSummary", "Level: $levelId Lessons: $totalLessons Stories: $totalStories Activities: $totalActivities Quizzes: $totalQuizzes")
                    val summary = LevelSummary(
                        levelName = levelId,
                        levelDescription = description,
                        totalChapters = totalLessons,
                        totalStories = totalStories,
                        totalQuizzes = totalQuizzes,
                        totalActivities = totalActivities
                    )
                    _levelSummary.value = summary
                } else {
                    Log.e("LevelSummary", "No chapters found for level: $levelId")
                    _levelSummary.value = null
                }
            } catch (e: Exception) {
                Log.e("LevelSummary", "Error fetching level summary: ${e.localizedMessage}")
                _levelSummary.value = null
            }
        }
    }
}