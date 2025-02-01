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
import kotlinx.coroutines.flow.asStateFlow
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

    private val _levelSummary = MutableStateFlow<LevelSummary?>(null)
    val levelSummary: StateFlow<LevelSummary?> = _levelSummary.asStateFlow()

    private val _levelName = MutableStateFlow<String?>(null)

    fun loadChapters(levelId: String) {
        val levelPath = "lessons/$levelId/chapters"  // ✅ Correct Firestore path

        Log.d("ChaptersViewModel", "Fetching chapters for level: $levelId from $levelPath")  // ✅ Debug log

        viewModelScope.launch {
            try {
                val userId = dataStoreManager.userId.first()
                if (userId.isNullOrEmpty()) {
                    Log.e("ChaptersViewModel", "User ID is null or empty")
                    return@launch
                }

                // Fetch chapters from Firestore
                val snapshot = fireStore.collection(levelPath).get().await()
                Log.d("ChaptersViewModel", "Fetched ${snapshot.documents.size} chapters") // ✅ Debugging log

                // Fetch completed chapters from user data
                val userDoc = fireStore.collection("users").document(userId).get().await()
                val completedChapters = userDoc.get("chapters_completed") as? List<String> ?: emptyList()

                if (snapshot.isEmpty) {
                    Log.e("ChaptersViewModel", "No chapters found in Firestore for $levelId")
                }

                val chapters = snapshot.documents.map { doc ->
                    Chapter(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        title = doc.getString("title") ?: "Untitled",
                        iconResId = R.drawable.start,
                        isCompleted = completedChapters.contains(doc.id),
                        isLocked = false, // Set unlock logic later
                        isUnlocked = true, // First chapter always unlocked
                        chapterType = doc.getString("chapterType") ?: ""
                    )
                }

                _chapters.value = chapters
                Log.d("ChaptersViewModel", "Loaded ${chapters.size} chapters")  // ✅ Debugging log

            } catch (e: Exception) {
                Log.e("ChaptersViewModel", "Error fetching chapters: ${e.localizedMessage}")
            }
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
                    checkAndAwardBadges(userId)
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


    private fun updateChapterStates(completedChapters: List<String>, chapters: List<Chapter>): List<Chapter> {
        return chapters.mapIndexed { index, chapter ->
            chapter.copy(
                isCompleted = completedChapters.contains(chapter.id.toString()),
                isLocked = when {
                    // First chapter is never locked
                    index == 0 -> false
                    // If previous chapter is completed, unlock this one
                    index > 0 && completedChapters.contains(chapters[index - 1].id.toString()) -> false
                    // Otherwise keep it locked
                    else -> true
                },
                isUnlocked = when {
                    // First chapter is always unlocked
                    index == 0 -> true
                    // If previous chapter is completed, unlock this one
                    index > 0 && completedChapters.contains(chapters[index - 1].id.toString()) -> true
                    // Otherwise keep it locked
                    else -> false
                }
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

                checkAndAwardBadges(userId)
                val userRef = fireStore.collection("users").document(userId)
                val levelPath = "lessons/$levelId/chapters"

                // Fetch all chapters and stories for the level
                val levelContentsSnapshot = fireStore.collection(levelPath).get().await()
                val levelContents = levelContentsSnapshot.documents.mapNotNull { doc ->
                    val type = doc.getString("chapterType") ?: return@mapNotNull null
                    val id = doc.getLong("id")?.toString() ?: return@mapNotNull null
                    id to type
                }

                fireStore.runTransaction { transaction ->
                    val userSnapshot = transaction.get(userRef)

                    // Update completed chapters or stories
                    when (chapterType) {
                        "Lesson" -> {
                            val completedChapters = userSnapshot.get("chapters_completed") as? MutableList<String>
                                ?: mutableListOf()
                            if (!completedChapters.contains(chapterId)) {
                                completedChapters.add(chapterId)
                                transaction.update(userRef, "chapters_completed", completedChapters)
                                val currentXP = userSnapshot.getLong("xp") ?: 0
                                transaction.update(userRef, "xp", currentXP + earnedXP)
                            }
                        }
                        "Story" -> {
                            val completedStories = userSnapshot.get("stories_completed") as? MutableList<String>
                                ?: mutableListOf()
                            if (!completedStories.contains(chapterId)) {
                                completedStories.add(chapterId)
                                transaction.update(userRef, "stories_completed", completedStories)
                                val currentXP = userSnapshot.getLong("xp") ?: 0
                                transaction.update(userRef, "xp", currentXP + earnedXP)
                            }
                        }
                    }

                    // Count required and completed chapters and stories
                    val requiredChapters = levelContents.count { it.second == "Lesson" }
                    val requiredStories = levelContents.count { it.second == "Story" }
                    val requiredActivities = levelContents.count{it.second == "Alphabet"}

                    val completedChapters = userSnapshot.get("chapters_completed") as? List<String> ?: emptyList()
                    val completedStories = userSnapshot.get("stories_completed") as? List<String> ?: emptyList()

                    val completedChaptersInLevel = levelContents
                        .filter { it.second == "Lesson" }
                        .count { completedChapters.contains(it.first) }

                    val completedStoriesInLevel = levelContents
                        .filter { it.second == "Story" }
                        .count { completedStories.contains(it.first) }

                    // Check if the level is fully completed
                    if (completedChaptersInLevel >= requiredChapters && completedStoriesInLevel >= requiredStories) {
                        val completedLevels = userSnapshot.get("levels_completed") as? MutableList<String>
                            ?: mutableListOf()

                        if (!completedLevels.contains(levelId)) {
                            completedLevels.add(levelId)
                            transaction.update(userRef, "levels_completed", completedLevels)

                            // Add bonus XP
                            val currentXP = userSnapshot.getLong("xp") ?: 0
                            transaction.update(userRef, "xp", currentXP + earnedXP + 100) // Assuming 100 as bonus XP

                            Log.d("LevelCompletion", """
                            Level $levelId completed!
                            Required Chapters: $requiredChapters
                            Completed Chapters: $completedChaptersInLevel
                            Required Stories: $requiredStories
                            Completed Stories: $completedStoriesInLevel
                            Bonus XP: 100
                        """.trimIndent())
                        }
                    }
                }.await()

                // Update streak and reload chapters
                updateStreak()
                loadChapters(levelId)
            } catch (e: Exception) {
                Log.e("ChaptersViewModel", "Error marking completion: ${e.localizedMessage}")
            }
        }
    }
    private suspend fun checkAndAwardBadges(userId: String) {
        try {
            val userRef = fireStore.collection("users").document(userId)
            val userDoc = userRef.get().await()

            // Get current earned badges
            val earnedBadges = userDoc.get("earned_badges") as? List<String> ?: emptyList()

            // Get all available badges
            val badgesSnapshot = fireStore.collection("badges").get().await()
            val badges = badgesSnapshot.documents.mapNotNull { it.toObject(Badge::class.java) }

            // Get user stats
            val quizzesAttended = userDoc.getLong("quizzes_attended") ?: 0
            val storiesCompleted = (userDoc.get("stories_completed") as? List<*>)?.size ?: 0
            val chaptersCompleted = (userDoc.get("chapters_completed") as? List<*>)?.size ?: 0
            val levelsCompleted = (userDoc.get("levels_completed") as? List<*>)?.size ?: 0
            val dayStreak = userDoc.getLong("day_streak") ?: 0
            val xp = userDoc.getLong("xp") ?: 0

            badges.forEach { badge ->
                // Skip if already earned
                if (!earnedBadges.contains(badge.id)) {
                    val shouldAward = when (badge.criteria.type) {
                        "quiz_completion" -> quizzesAttended >= (badge.criteria.count ?: 0)
                        "story_completion" -> storiesCompleted >= (badge.criteria.count ?: 0)
                        "chapter_completion" -> chaptersCompleted >= (badge.criteria.count ?: 0)
                        "level_completion" -> levelsCompleted >= (badge.criteria.count ?: 0)
                        "streak" -> dayStreak >= (badge.criteria.count ?: 0)
                        "xp_earned" -> xp >= (badge.criteria.count ?: 0)
                        else -> false
                    }

                    if (shouldAward) {
                        // Add to earned_badges array
                        userRef.update("earned_badges", earnedBadges + badge.id).await()

                        // Notify UI about earned badge
                        _badgeEarnedEvent.value = badge

                        Log.d("BadgeCheck", "Badge earned: ${badge.title}")
                    }
                }
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

                // Remove the "level" prefix from the path since levelId already includes it
                val levelPath = "lessons/$levelId"  // Changed from "lessons/level$levelId"
                val levelDoc = fireStore.document(levelPath).get().await()
                val description = levelDoc.getString("description") ?: "No description available"

                // Fetch all chapters for the level
                val chaptersSnapshot = fireStore.collection("$levelPath/chapters").get().await()

                if (!chaptersSnapshot.isEmpty) {
                    var totalLessons = 0
                    var totalStories = 0
                    var totalQuizzes = 0
                    var totalActivities = 0

                    // Calculate totals based on chapter type
                    chaptersSnapshot.documents.forEach { doc ->
                        when (doc.getString("chapterType")) {
                            "Lesson" -> totalLessons++
                            "Story" -> totalStories++
                            "Quiz" -> totalQuizzes++
                            "Alphabet" -> totalActivities++
                        }
                    }

                    Log.d("LevelSummary", """
                    Level: $levelId
                    Lessons: $totalLessons
                    Stories: $totalStories
                    Activities: $totalActivities
                    Quizzes: $totalQuizzes
                """.trimIndent())

                    // Update LevelSummary
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
