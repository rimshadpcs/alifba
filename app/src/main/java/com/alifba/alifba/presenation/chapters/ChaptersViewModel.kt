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

    private val _badgeEarnedEvent = MutableStateFlow<Badge?>(null)
    val badgeEarnedEvent: StateFlow<Badge?> get() = _badgeEarnedEvent

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

                // Fetch completed chapters from user data
                val userDoc = fireStore.collection("users").document(userId).get().await()
                val completedChapters = userDoc.get("chapters_completed") as? List<String> ?: emptyList()

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
                            "Alphabet" -> R.drawable.alphabeticon
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
        _badgeEarnedEvent.value = null
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
                    val currentStreak = snapshot.getLong("day_streak") ?: 0
                    val currentDate = getCurrentDate()
                    if (lastActivityDate != currentDate) {
                        val newStreak = if (isYesterday(lastActivityDate)) currentStreak + 1 else 1
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

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun isYesterday(lastDate: String): Boolean {
        return false
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
                        "Alphabet" -> R.drawable.alphabeticon
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

                checkAndAwardBadges(userId)
                val userRef = fireStore.collection("users").document(userId)
                val levelPath = "lessons/$levelId/chapters"
                // Fetch all chapters for level (for level-completion logic)
                val levelContentsSnapshot = fireStore.collection(levelPath).get().await()
                val levelContents = levelContentsSnapshot.documents.mapNotNull { doc ->
                    val type = doc.getString("chapterType") ?: return@mapNotNull null
                    val id = doc.getLong("id")?.toString() ?: return@mapNotNull null
                    id to type
                }

                fireStore.runTransaction { transaction ->
                    val userSnapshot = transaction.get(userRef)

                    // Update category-specific completions
                    val lessonsCompleted = userSnapshot.get("lessons_completed") as? MutableList<String> ?: mutableListOf()
                    if (chapterType == "Lesson" && !lessonsCompleted.contains(chapterId)) {
                        lessonsCompleted.add(chapterId)
                        transaction.update(userRef, "lessons_completed", lessonsCompleted)
                    }

                    val storiesCompleted = userSnapshot.get("stories_completed") as? MutableList<String> ?: mutableListOf()
                    if (chapterType == "Story" && !storiesCompleted.contains(chapterId)) {
                        storiesCompleted.add(chapterId)
                        transaction.update(userRef, "stories_completed", storiesCompleted)
                    }

                    val activitiesCompleted = userSnapshot.get("activities_completed") as? MutableList<String> ?: mutableListOf()
                    if (chapterType == "Alphabet" && !activitiesCompleted.contains(chapterId)) {
                        activitiesCompleted.add(chapterId)
                        transaction.update(userRef, "activities_completed", activitiesCompleted)
                    }

                    // Update general completion (chapters_completed)
                    val allChaptersCompleted = userSnapshot.get("chapters_completed") as? MutableList<String> ?: mutableListOf()
                    if (!allChaptersCompleted.contains(chapterId)) {
                        allChaptersCompleted.add(chapterId)
                        transaction.update(userRef, "chapters_completed", allChaptersCompleted)
                        val currentXP = userSnapshot.getLong("xp") ?: 0
                        transaction.update(userRef, "xp", currentXP + earnedXP)
                    }

                    // Level completion logic
                    val requiredChapters = levelContents.count { it.second == "Lesson" }
                    val requiredStories = levelContents.count { it.second == "Story" }
                    val completedChaptersInLevel = levelContents.filter { it.second == "Lesson" }
                        .count { allChaptersCompleted.contains(it.first) }
                    val completedStoriesInLevel = levelContents.filter { it.second == "Story" }
                        .count { storiesCompleted.contains(it.first) }
                    if (completedChaptersInLevel >= requiredChapters && completedStoriesInLevel >= requiredStories) {
                        val completedLevels = userSnapshot.get("levels_completed") as? MutableList<String> ?: mutableListOf()
                        if (!completedLevels.contains(levelId)) {
                            completedLevels.add(levelId)
                            transaction.update(userRef, "levels_completed", completedLevels)
                            val currentXP = userSnapshot.getLong("xp") ?: 0
                            transaction.update(userRef, "xp", currentXP + earnedXP + 100)
                            Log.d("LevelCompletion", "Level $levelId completed! Bonus XP: 100")
                        }
                    }
                }.await()

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
            val earnedBadges = userDoc.get("earned_badges") as? List<String> ?: emptyList()
            val badgesSnapshot = fireStore.collection("badges").get().await()
            val badges = badgesSnapshot.documents.mapNotNull { it.toObject(Badge::class.java) }
            val quizzesAttended = userDoc.getLong("quizzes_attended") ?: 0
            val storiesCount = (userDoc.get("stories_completed") as? List<*>)?.size ?: 0
            val chaptersCount = (userDoc.get("chapters_completed") as? List<*>)?.size ?: 0
            val levelsCount = (userDoc.get("levels_completed") as? List<*>)?.size ?: 0
            val dayStreak = userDoc.getLong("day_streak") ?: 0
            val xp = userDoc.getLong("xp") ?: 0

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
                        userRef.update("earned_badges", earnedBadges + badge.id).await()
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