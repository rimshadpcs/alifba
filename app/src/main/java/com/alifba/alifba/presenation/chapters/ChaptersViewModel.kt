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
import com.alifba.alifba.utils.ReviewPromptMilestone
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

    // One-shot signal for a review-prompt value moment (first lesson completed, first 7-day
    // streak, first badge earned) just having happened for the given profile. Detection lives
    // here since it needs the before/after Firestore state this ViewModel already reads; the
    // actual ReviewPromptManager call (which needs a real Activity for Play's In-App Review
    // API, not just ApplicationContext) happens at the UI layer observing this — same split as
    // badgeEarnedEvent above. ReviewPromptManager itself is the one source of truth for "have
    // we already fired this milestone" and the weekly attempt cooldown; this ViewModel doesn't
    // dedupe beyond what's naturally one-shot in the underlying Firestore transition (e.g. a
    // streak can cross 7 again after resetting, so that one alone isn't self-deduping).
    private val _reviewPromptTrigger = MutableStateFlow<ReviewPromptMilestone?>(null)
    val reviewPromptTrigger: StateFlow<ReviewPromptMilestone?> get() = _reviewPromptTrigger

    fun clearReviewPromptTrigger() {
        _reviewPromptTrigger.value = null
    }

    private val _levelSummary = MutableStateFlow<LevelSummary?>(null)
    val levelSummary: StateFlow<LevelSummary?> = _levelSummary.asStateFlow()

    fun loadChapters(levelId: String) {
        // levelId kept for backwards compatibility; chapters are now top-level
        val collectionPath = "chapters"
        Log.d("ChaptersViewModel", "Fetching chapters from top-level: $collectionPath (levelId=$levelId)")
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.userId.first()
                if (userId.isNullOrEmpty()) {
                    Log.e("ChaptersViewModel", "User ID is null or empty")
                    return@launch
                }

                // Fetch chapters from Firestore (top-level)
                val snapshot = fireStore.collection(collectionPath).get().await()
                Log.d("ChaptersViewModel", "Fetched ${snapshot.documents.size} chapters from $collectionPath")

                // Fetch completed chapters from user data - from profiles array
                val userDoc = fireStore.collection("users").document(userId).get().await()
                val profiles = userDoc.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                val deviceActiveProfileId = getDeviceActiveProfileId(userId)
                val isPremium = dataStoreManager.isPremium.value
                val legacyIndex = (userDoc.getLong("activeProfileIndex") ?: 0).toInt()
                val activeProfileIndex = resolveActiveProfileIndex(
                    profiles = profiles,
                    deviceActiveProfileId = deviceActiveProfileId,
                    legacyIndex = legacyIndex,
                    isPremium = isPremium
                )
                val completedChapters = if (profiles.isNotEmpty() && activeProfileIndex < profiles.size) {
                    profiles[activeProfileIndex]["chaptersCompleted"] as? List<String> ?: emptyList()
                } else {
                    emptyList()
                }

                if (snapshot.isEmpty) {
                    Log.e("ChaptersViewModel", "No chapters found in Firestore for $levelId")
                }

                val chaptersRaw = snapshot.documents.map { doc ->
                    val chapterTypeRaw = doc.getString("chapterType") ?: ""
                    val numericId = doc.getLong("id")?.toInt() ?: 0
                    Chapter(
                        id = numericId,
                        title = doc.getString("title") ?: "Untitled",
                        // Use the numeric id (converted to string) for consistency
                        isCompleted = completedChapters.contains(numericId.toString()),
                        isLocked = true,
                        isUnlocked = false,
                        chapterType = chapterTypeRaw
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

    // Ported from ChaptersViewModel.swift's updateStreak() so both platforms apply the same
    // day-streak rule against the same Firestore fields (per-profile lastActivityDate/dayStreak
    // — not the old top-level last_activity_date key, which nothing else ever read). Same day
    // as last activity is a no-op, exactly one day later increments, anything else (including
    // the very first-ever activity, when lastActivityDate is empty) resets to 1.
    private fun updateStreak() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (userId.isNullOrEmpty()) return@launch
            try {
                val deviceActiveProfileId = getDeviceActiveProfileId(userId)
                val isPremium = dataStoreManager.isPremium.value
                val userRef = fireStore.collection("users").document(userId)
                var crossedSevenDayStreak = false
                var streakProfileId: String? = null
                fireStore.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentDate = getCurrentDate()

                    // Get profiles array and update day streak for active profile
                    val profiles = snapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                    if (profiles.isNotEmpty()) {
                        val legacyIndex = (snapshot.getLong("activeProfileIndex") ?: 0).toInt()
                        val activeProfileIndex = resolveActiveProfileIndex(
                            profiles = profiles,
                            deviceActiveProfileId = deviceActiveProfileId,
                            legacyIndex = legacyIndex,
                            isPremium = isPremium
                        )
                        if (activeProfileIndex < profiles.size) {
                            val updatedProfiles = profiles.toMutableList()
                            val currentProfile = updatedProfiles[activeProfileIndex].toMutableMap()
                            val lastActivityDate = currentProfile["lastActivityDate"] as? String ?: ""
                            val currentStreak = (currentProfile["dayStreak"] as? Long)?.toInt() ?: 0

                            val newStreak = computeNextDayStreak(lastActivityDate, currentDate, currentStreak)

                            Log.d("StreakDebug", "Last activity: '$lastActivityDate', Current date: '$currentDate', Current streak: $currentStreak, New streak: $newStreak")

                            if (currentStreak < 7 && newStreak >= 7) {
                                crossedSevenDayStreak = true
                                streakProfileId = currentProfile["profileId"] as? String
                            }

                            if (newStreak != currentStreak || lastActivityDate != currentDate) {
                                currentProfile["dayStreak"] = newStreak
                                currentProfile["lastActivityDate"] = currentDate
                                updatedProfiles[activeProfileIndex] = currentProfile

                                transaction.update(userRef, "profiles", updatedProfiles)
                                transaction.update(userRef, "lastUpdated", System.currentTimeMillis())
                            }
                        }
                    }
                }.await()

                if (crossedSevenDayStreak && streakProfileId != null && _reviewPromptTrigger.value == null) {
                    _reviewPromptTrigger.value = ReviewPromptMilestone(
                        type = ReviewPromptMilestone.Type.FIRST_STREAK,
                        profileId = streakProfileId!!
                    )
                }
            } catch (e: Exception) {
                Log.e("ChaptersViewModel", "Error updating streak: ${e.localizedMessage}")
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private suspend fun getDeviceActiveProfileId(userId: String): String? {
        val deviceId = dataStoreManager.getOrCreateDeviceId()
        val deviceSnapshot = fireStore.collection("users")
            .document(userId)
            .collection("devices")
            .document(deviceId)
            .get()
            .await()
        return deviceSnapshot.getString("activeProfileId")
    }

    private fun resolveActiveProfileIndex(
        profiles: List<Map<String, Any>>,
        deviceActiveProfileId: String?,
        legacyIndex: Int,
        isPremium: Boolean
    ): Int {
        if (profiles.isEmpty()) return 0
        val legacySafeIndex = legacyIndex.coerceIn(0, profiles.lastIndex)
        val deviceIndex = if (!deviceActiveProfileId.isNullOrBlank()) {
            profiles.indexOfFirst { it["profileId"] == deviceActiveProfileId }
        } else {
            -1
        }
        val resolvedIndex = if (deviceIndex >= 0) deviceIndex else legacySafeIndex
        return if (!isPremium && resolvedIndex > 0) 0 else resolvedIndex
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
            chapter.copy(
                isCompleted = isCompleted,
                isLocked = isLocked,
                isUnlocked = !isLocked
            )
        }
    }

    suspend fun checkAndMarkChapterCompletion(
        chapterId: String,
        levelId: String,
        earnedXP: Int,
        chapterType: String
    ): List<Badge> {
        return try {
            val userId = dataStoreManager.userId.first()
            if (userId.isNullOrEmpty()) return emptyList()

            android.util.Log.d("ChaptersViewModel", "Fast chapter completion update for: $chapterId")
            val userRef = fireStore.collection("users").document(userId)

            var isNewLimitedCompletion = false
            var profileIdForLimit: String? = null
            var lifetimeLimitedCompletedCountAfter = 0
            var isFirstLessonCompletion = false
            val deviceActiveProfileId = getDeviceActiveProfileId(userId)
            val isPremium = dataStoreManager.isPremium.value

            // STREAMLINED: Single transaction for immediate update
            fireStore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)

                // Get current profiles array
                val profiles = userSnapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                if (profiles.isEmpty()) return@runTransaction

                val legacyIndex = (userSnapshot.getLong("activeProfileIndex") ?: 0).toInt()
                val activeProfileIndex = resolveActiveProfileIndex(
                    profiles = profiles,
                    deviceActiveProfileId = deviceActiveProfileId,
                    legacyIndex = legacyIndex,
                    isPremium = isPremium
                )
                if (activeProfileIndex >= profiles.size) return@runTransaction

                val updatedProfiles = profiles.toMutableList()
                val currentProfile = updatedProfiles[activeProfileIndex].toMutableMap()

                profileIdForLimit = currentProfile["profileId"] as? String

                val chapterTypeKey = chapterType.lowercase()

                // Update completion lists (defensively handle existing list types)
                val allChaptersCompleted = (currentProfile["chaptersCompleted"] as? List<*>)
                    ?.mapNotNull { it?.toString() }
                    ?.toMutableList()
                    ?: mutableListOf()
                if (!allChaptersCompleted.contains(chapterId)) {
                    allChaptersCompleted.add(chapterId)
                    currentProfile["chaptersCompleted"] = allChaptersCompleted

                    if (chapterTypeKey == "lesson" || chapterTypeKey == "story") {
                        isNewLimitedCompletion = true
                    }
                }

                // Update XP
                val currentXP = (currentProfile["xp"] as? Long)?.toInt() ?: 0
                currentProfile["xp"] = currentXP + earnedXP

                // Update category-specific lists
                when (chapterTypeKey) {
                    "lesson" -> {
                        val lessonsCompleted = (currentProfile["lessonsCompleted"] as? List<*>)
                            ?.mapNotNull { it?.toString() }
                            ?.toMutableList()
                            ?: mutableListOf()
                        if (!lessonsCompleted.contains(chapterId)) {
                            // Checked before adding — true only when this profile had zero
                            // lessons completed until now.
                            isFirstLessonCompletion = lessonsCompleted.isEmpty()
                            lessonsCompleted.add(chapterId)
                            currentProfile["lessonsCompleted"] = lessonsCompleted
                        }
                    }
                    "story" -> {
                        val storiesCompleted = (currentProfile["storiesCompleted"] as? List<*>)
                            ?.mapNotNull { it?.toString() }
                            ?.toMutableList()
                            ?: mutableListOf()
                        if (!storiesCompleted.contains(chapterId)) {
                            storiesCompleted.add(chapterId)
                            currentProfile["storiesCompleted"] = storiesCompleted
                        }
                    }
                    "alphabet" -> {
                        val activitiesCompleted = (currentProfile["activitiesCompleted"] as? List<*>)
                            ?.mapNotNull { it?.toString() }
                            ?.toMutableList()
                            ?: mutableListOf()
                        if (!activitiesCompleted.contains(chapterId)) {
                            activitiesCompleted.add(chapterId)
                            currentProfile["activitiesCompleted"] = activitiesCompleted
                        }
                    }
                }

                // Compute total limited (lesson + story) completions after this update
                val lessonsCompletedList = (currentProfile["lessonsCompleted"] as? List<*>)
                    ?.mapNotNull { it?.toString() }
                    ?: emptyList()
                val storiesCompletedList = (currentProfile["storiesCompleted"] as? List<*>)
                    ?.mapNotNull { it?.toString() }
                    ?: emptyList()
                lifetimeLimitedCompletedCountAfter = lessonsCompletedList.size + storiesCompletedList.size

                updatedProfiles[activeProfileIndex] = currentProfile

                // Single transaction update
                transaction.update(userRef, "profiles", updatedProfiles)
                transaction.update(userRef, "lastUpdated", System.currentTimeMillis())
            }.await()

            android.util.Log.d("ChaptersViewModel", "Fast chapter completion completed")

            // Increment local daily completion counter only when a new limited chapter is completed
            if (isNewLimitedCompletion && profileIdForLimit != null) {
                try {
                    val baseLimit = dataStoreManager.getEffectiveDailyLessonLimit()
                    val maxPerDay = dataStoreManager.adjustDailyLimitForProgress(
                        baseLimit = baseLimit,
                        lifetimeLimitedCompletedCount = lifetimeLimitedCompletedCountAfter
                    )
                    dataStoreManager.tryConsumeDailyLessonSlot(
                        profileId = profileIdForLimit!!,
                        maxPerDay = maxPerDay
                    )
                } catch (e: Exception) {
                    Log.e("ChaptersViewModel", "Error updating local daily lesson counter: ${e.localizedMessage}")
                }
            }

            if (isFirstLessonCompletion && profileIdForLimit != null && _reviewPromptTrigger.value == null) {
                _reviewPromptTrigger.value = ReviewPromptMilestone(
                    type = ReviewPromptMilestone.Type.FIRST_LESSON,
                    profileId = profileIdForLimit!!
                )
            }

            // Captured before updateStreak() rather than returned directly, since
            // updateStreak() (matching ChaptersViewModel.swift's own call order — badge check
            // first, streak update after) would otherwise become this try block's last
            // expression and silently change the function's return value.
            val newlyEarnedBadges = checkAndAwardBadges(userId)
            updateStreak()
            newlyEarnedBadges
        } catch (e: Exception) {
            Log.e("ChaptersViewModel", "Error in fast chapter completion: ${e.localizedMessage}")
            emptyList()
        }
    }

    fun checkAndMarkChapterCompletionAsync(
        chapterId: String,
        levelId: String,
        earnedXP: Int,
        chapterType: String
    ) {
        viewModelScope.launch {
            checkAndMarkChapterCompletion(
                chapterId = chapterId,
                levelId = levelId,
                earnedXP = earnedXP,
                chapterType = chapterType
            )
        }
    }

    private suspend fun checkAndAwardBadges(userId: String): List<Badge> {
        return try {
            val userRef = fireStore.collection("users").document(userId)
            val deviceActiveProfileId = getDeviceActiveProfileId(userId)
            val isPremium = dataStoreManager.isPremium.value

            // Fetch all badge definitions once (outside of user transaction)
            val badgesSnapshot = fireStore.collection("badges").get().await()
            val allBadges = badgesSnapshot.documents.mapNotNull { it.toObject(Badge::class.java) }

            if (allBadges.isEmpty()) {
                Log.d("BadgeCheck", "No badges configured in Firestore")
                return emptyList()
            }

            val newlyEarnedBadges = mutableListOf<Badge>()
            var isFirstBadgeEarned = false
            var badgeProfileId: String? = null

            fireStore.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)

                // Get stats from profiles array
                val profiles = userDoc.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                if (profiles.isEmpty()) return@runTransaction

                val legacyIndex = (userDoc.getLong("activeProfileIndex") ?: 0).toInt()
                val activeProfileIndex = resolveActiveProfileIndex(
                    profiles = profiles,
                    deviceActiveProfileId = deviceActiveProfileId,
                    legacyIndex = legacyIndex,
                    isPremium = isPremium
                )
                if (activeProfileIndex >= profiles.size) return@runTransaction

                val updatedProfiles = profiles.toMutableList()
                val currentProfile = updatedProfiles[activeProfileIndex].toMutableMap()

                val earnedBadges = currentProfile["earnedBadges"] as? List<String> ?: emptyList()

                val quizzesAttended = (currentProfile["quizzesAttended"] as? Long) ?: 0
                val storiesCount = (currentProfile["storiesCompleted"] as? List<*>)?.size ?: 0
                val chaptersCount = (currentProfile["chaptersCompleted"] as? List<*>)?.size ?: 0
                val levelsCount = (currentProfile["levelsCompleted"] as? List<*>)?.size ?: 0
                val dayStreak = (currentProfile["dayStreak"] as? Long) ?: 0
                val xp = (currentProfile["xp"] as? Long) ?: 0

                val badgeIdsToAdd = mutableListOf<String>()

                allBadges.forEach { badge ->
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

                if (badgeIdsToAdd.isNotEmpty()) {
                    if (earnedBadges.isEmpty()) {
                        isFirstBadgeEarned = true
                        badgeProfileId = currentProfile["profileId"] as? String
                    }
                    val updatedEarnedBadges = (currentProfile["earnedBadges"] as? List<String> ?: emptyList()).toMutableList()
                    updatedEarnedBadges.addAll(badgeIdsToAdd)
                    currentProfile["earnedBadges"] = updatedEarnedBadges
                    updatedProfiles[activeProfileIndex] = currentProfile

                    transaction.update(
                        userRef,
                        mapOf(
                            "profiles" to updatedProfiles,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                    )
                }
            }.await()

            if (newlyEarnedBadges.isNotEmpty()) {
                _badgeEarnedEvent.value = newlyEarnedBadges
                Log.d("BadgeCheck", "Total badges earned: ${newlyEarnedBadges.size}")
            }
            if (isFirstBadgeEarned && badgeProfileId != null && _reviewPromptTrigger.value == null) {
                _reviewPromptTrigger.value = ReviewPromptMilestone(
                    type = ReviewPromptMilestone.Type.FIRST_BADGE,
                    profileId = badgeProfileId!!
                )
            }
            newlyEarnedBadges
        } catch (e: Exception) {
            Log.e("BadgeCheck", "Error checking badges: ${e.localizedMessage}")
            emptyList()
        }
    }

    fun getLevelSummary(levelId: String) {
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.userId.first()
                if (userId.isNullOrEmpty()) return@launch
                // Levels are deprecated; we still optionally read level meta if present,
                // but chapter stats now come from the top-level chapters collection.
                val levelPath = "lessons/$levelId"
                val levelDoc = fireStore.document(levelPath).get().await()
                val description = levelDoc.getString("description") ?: "No description available"

                val chaptersSnapshot = fireStore.collection("chapters").get().await()
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
