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
import kotlinx.coroutines.flow.filter
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
        LevelItem("Level 1", R.drawable.privacy_policy, "level1"),
        LevelItem("Level 2",R.drawable.privacy_policy,"level2"),
        LevelItem("Level 3", R.drawable.privacy_policy, "level3"),
        LevelItem("Level 4",R.drawable.privacy_policy,"level4"),
        LevelItem("Level 5",R.drawable.privacy_policy,"level5"),
        LevelItem("Level 6", R.drawable.privacy_policy, "level6"),
        LevelItem("Level 7",R.drawable.privacy_policy,"level7"),
        LevelItem("Level 8",R.drawable.privacy_policy,"level8"),
        LevelItem("Level 9", R.drawable.privacy_policy, "level9"),
        LevelItem("Level 10",R.drawable.privacy_policy,"level10"),
    )
    
    // Chapter loading functionality
    private val _chapters = MutableLiveData<List<Chapter>>()
    val chapters: LiveData<List<Chapter>> = _chapters
    
    val chapterStatuses: StateFlow<Map<String, Boolean>> = dataStoreManager.getChapterStatuses()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val appOpenCount: StateFlow<Int> = dataStoreManager.appOpenCount
    val lastPaywallOpenCount: StateFlow<Int> = dataStoreManager.lastPaywallOpenCount
    val hasShownHomePaywall: StateFlow<Boolean> = dataStoreManager.hasShownHomePaywall
    
    private val _badgeEarnedEvent = MutableStateFlow<Badge?>(null)
    val badgeEarnedEvent: StateFlow<Badge?> get() = _badgeEarnedEvent
    
    // Auto-load chapters on initialization (levels deprecated)
    init {
        listenForChapterUpdates()
    }
    
    private fun loadLevel1Chapters() {
        listenForChapterUpdates("level1")
    }

    fun markHomePaywallShown() {
        viewModelScope.launch {
            dataStoreManager.setHasShownHomePaywall(true)
        }
    }

    fun markPaywallShownForOpen(openCount: Int) {
        viewModelScope.launch {
            dataStoreManager.setLastPaywallOpenCount(openCount)
        }
    }
    
    fun listenForChapterUpdates(levelId: String = "level1", retryCount: Int = 0) {
        // levelId kept only for backwards compatibility; chapters are now top-level
        viewModelScope.launch {
            // dataStoreManager.userId is a StateFlow reactively backed by DataStore itself, so
            // rather than grabbing whatever's there right now (which right after onboarding can
            // still be null — DataStore's write hasn't necessarily committed by the moment this
            // ViewModel's init{} runs) and giving up permanently, actually wait for DataStore to
            // emit the real value, however long that takes. No arbitrary delay/timeout needed.
            val userId = dataStoreManager.userId.filter { !it.isNullOrEmpty() }.first()!!

            val deviceId = dataStoreManager.getOrCreateDeviceId()
            val userDocRef = fireStore.collection("users").document(userId)
            userDocRef.addSnapshotListener { userSnapshot, e ->
                if (e != null) {
                    // Right after onboarding/sign-in, Firebase Auth's ID token can take a moment
                    // to propagate to Firestore's client, so this very first listener attach can
                    // fail with a permission error even though the user is genuinely signed in.
                    // A Firestore snapshot listener does not self-heal once it errors — it stays
                    // dead until a new one is attached — which is why re-attaching here (rather
                    // than just logging) is what actually fixes the "only a fresh launch works"
                    // symptom instead of requiring the user to restart the app.
                    Log.w("HomeViewModel", "Listen failed (attempt ${retryCount + 1}).", e)
                    if (retryCount < 5) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(1000L * (retryCount + 1))
                            listenForChapterUpdates(levelId, retryCount + 1)
                        }
                    }
                    return@addSnapshotListener
                }

                if (userSnapshot != null && userSnapshot.exists()) {
                    Log.d("HomeViewModel", "Snapshot listener triggered")
                    viewModelScope.launch {
                        val profiles = userSnapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                        val deviceSnapshot = fireStore.collection("users")
                            .document(userId)
                            .collection("devices")
                            .document(deviceId)
                            .get()
                            .await()
                        val deviceActiveProfileId = deviceSnapshot.getString("activeProfileId")
                        val isPremium = dataStoreManager.isPremium.value
                        val legacyIndex = (userSnapshot.getLong("activeProfileIndex") ?: 0).toInt()
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
                        Log.d("HomeViewModel", "Updated completedChapters: $completedChapters")
                        loadChapters(completedChapters)
                    }
                } else {
                    Log.d("HomeViewModel", "Current data: null")
                }
            }
        }
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

    private fun loadChapters(completedChapters: List<String>, retryCount: Int = 0) {
        viewModelScope.launch {
            try {
                val snapshot = fireStore.collection("chapters").get().await()
                Log.d("HomeViewModel", "Fetched ${snapshot.documents.size} chapters from top-level collection")

                Log.d(
                    "HomeViewModel",
                    "Chapter documents loaded: ids=${snapshot.documents.map { it.id }}, paths=${snapshot.documents.map { it.reference.path }}"
                )

                val chaptersRaw = snapshot.documents.map { doc ->
                    val chapterTypeRaw = doc.getString("chapterType") ?: ""
                    val numericId = doc.getLong("id")?.toInt() ?: 0
                    Chapter(
                        id = numericId,
                        title = doc.getString("title") ?: "Untitled",
                        isCompleted = completedChapters.contains(numericId.toString()),
                        isLocked = true,
                        isUnlocked = false,
                        chapterType = chapterTypeRaw
                    )
                }.sortedBy { it.id }

                val updatedChapters = updateChapterStates(completedChapters, chaptersRaw)
                _chapters.value = updatedChapters
                Log.d("HomeViewModel", "_chapters LiveData updated")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching chapters (attempt ${retryCount + 1}): ${e.localizedMessage}")
                if (retryCount < 5) {
                    kotlinx.coroutines.delay(1000L * (retryCount + 1))
                    loadChapters(completedChapters, retryCount + 1)
                }
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
            chapter.copy(
                isCompleted = isCompleted,
                isLocked = isLocked,
                isUnlocked = !isLocked
            )
        }
    }
    
    fun clearBadgeEvent() {
        _badgeEarnedEvent.value = null
    }
}
