package com.alifba.alifba.presenation.home.layout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.presenation.login.ParentAccount
import com.alifba.alifba.presenation.login.ChildProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject



@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val fireStore: FirebaseFirestore
) : ViewModel() {

    private val _parentAccountState = MutableStateFlow<ParentAccount?>(null)
    val parentAccountState: StateFlow<ParentAccount?> get() = _parentAccountState
    
    private val _currentChildProfile = MutableStateFlow<ChildProfile?>(null)
    val currentChildProfile: StateFlow<ChildProfile?> get() = _currentChildProfile
    private val _earnedBadges = MutableStateFlow<List<Badge>>(emptyList())
    val earnedBadges: StateFlow<List<Badge>> get() = _earnedBadges

    // Badge caching to avoid re-fetching
    private val badgeCache = mutableMapOf<String, Badge>()
    private var lastFetchedBadgeIds: List<String> = emptyList()

    private var profileListenerRegistration: ListenerRegistration? = null
    private val _isLoading = MutableStateFlow(true)

    init {
        fetchUserProfile()
        // Pre-warm badge cache in background for better UX
        preWarmBadgeCache()
    }

    /**
     * Fetches all relevant user data (XP, chaptersCompleted, badges, child profile, etc.)
     * from Firestore and updates the multi-profile state.
     */
    private fun fetchUserProfile() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (!userId.isNullOrEmpty()) {
                try {
                    val documentSnapshot = fireStore.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    if (documentSnapshot.exists()) {
                        val deviceId = dataStoreManager.getOrCreateDeviceId()
                        val deviceSnapshot = fireStore.collection("users")
                            .document(userId)
                            .collection("devices")
                            .document(deviceId)
                            .get()
                            .await()
                        val deviceActiveProfileId = deviceSnapshot.getString("activeProfileId")

                        // Load multi-profile structure - ONLY from profiles array
                        val profiles = documentSnapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                        val premium = dataStoreManager.isPremium.value
                        val parentAccount = ParentAccount(
                            parentName = documentSnapshot.getString("parentName") ?: "",
                            primaryEmail = documentSnapshot.getString("primaryEmail")
                                ?: documentSnapshot.getString("email"),
                            contactEmail = documentSnapshot.getString("contactEmail"),
                            userId = documentSnapshot.getString("userId") ?: "",
                            profiles = profiles.map { profileMap ->
                                val profileId = profileMap["profileId"] as? String ?: java.util.UUID.randomUUID().toString()
                                ChildProfile(
                                    profileId = profileId,
                                    childName = profileMap["childName"] as? String ?: "",
                                    age = (profileMap["age"] as? Long)?.toInt() ?: 0,
                                    avatar = profileMap["avatar"] as? String ?: "",
                                    xp = (profileMap["xp"] as? Long)?.toInt() ?: 0,
                                    earnedBadges = profileMap["earnedBadges"] as? List<String> ?: emptyList(),
                                    chaptersCompleted = profileMap["chaptersCompleted"] as? List<String> ?: emptyList(),
                                    storiesCompleted = profileMap["storiesCompleted"] as? List<String> ?: emptyList(),
                                    levelsCompleted = profileMap["levelsCompleted"] as? List<String> ?: emptyList(),
                                    quizzesAttended = (profileMap["quizzesAttended"] as? Long)?.toInt() ?: 0,
                                    dayStreak = (profileMap["dayStreak"] as? Long)?.toInt() ?: 0,
                                    lessonsCompleted = profileMap["lessonsCompleted"] as? List<String> ?: emptyList(),
                                    activitiesCompleted = profileMap["activitiesCompleted"] as? List<String> ?: emptyList(),
                                    createdAt = profileMap["createdAt"] as? Long ?: System.currentTimeMillis()
                                )
                            },
                            activeProfileIndex = 0,
                            createdAt = documentSnapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                            lastUpdated = documentSnapshot.getLong("lastUpdated") ?: System.currentTimeMillis()
                        )

                        // Update local state
                        _parentAccountState.value = parentAccount

                        // Set the current active child profile and fetch badges
                        if (parentAccount.profiles.isNotEmpty()) {
                            val resolvedActiveProfileId = resolveActiveProfileId(
                                parentAccount = parentAccount,
                                deviceActiveProfileId = deviceActiveProfileId,
                                isPremium = premium,
                                legacyIndex = (documentSnapshot.getLong("activeProfileIndex") ?: 0).toInt()
                            )
                            val activeIndex = parentAccount.profiles.indexOfFirst { it.profileId == resolvedActiveProfileId }
                            val safeIndex = if (activeIndex >= 0) activeIndex else 0
                            val currentProfile = parentAccount.profiles[safeIndex]

                            _parentAccountState.value = parentAccount.copy(activeProfileIndex = safeIndex)
                            _currentChildProfile.value = currentProfile

                            if (!resolvedActiveProfileId.isNullOrBlank() && resolvedActiveProfileId != deviceActiveProfileId) {
                                fireStore.collection("users")
                                    .document(userId)
                                    .collection("devices")
                                    .document(deviceId)
                                    .set(
                                        mapOf(
                                            "activeProfileId" to resolvedActiveProfileId,
                                            "platform" to "Android",
                                            "lastSeen" to System.currentTimeMillis()
                                        ),
                                        com.google.firebase.firestore.SetOptions.merge()
                                    )
                                    .await()
                            }

                            // Fetch earned badges for the current profile
                            viewModelScope.launch {
                                fetchEarnedBadges(currentProfile.earnedBadges)
                            }
                        }
                    } else {
                        Log.d("ProfileViewModel", "User document does not exist in Firestore.")
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error fetching user profile: ${e.localizedMessage}")
                }
            } else {
                Log.d("ProfileViewModel", "No valid user ID found in DataStore.")
            }
        }
    }
    private suspend fun fetchEarnedBadges(earnedBadgeIds: List<String>) {
        try {
            if (earnedBadgeIds.isEmpty()) {
                _earnedBadges.value = emptyList()
                lastFetchedBadgeIds = emptyList()
                return
            }

            // Check if badge IDs haven't changed - if so, use cache
            if (earnedBadgeIds == lastFetchedBadgeIds && badgeCache.isNotEmpty()) {
                val cachedBadges = earnedBadgeIds.mapNotNull { badgeCache[it] }
                if (cachedBadges.size == earnedBadgeIds.size) {
                    _earnedBadges.value = cachedBadges
                    Log.d("ProfileViewModel", "Using cached badges: ${cachedBadges.size} badges")
                    return
                }
            }

            Log.d("ProfileViewModel", "Fetching badges from Firestore: $earnedBadgeIds")
            val badgesCollection = fireStore.collection("badges")

            // Check cache first, only fetch missing badges
            val badgesToFetch = earnedBadgeIds.filter { !badgeCache.containsKey(it) }

            // Fetch missing badges
            if (badgesToFetch.isNotEmpty()) {
                val newBadges = badgesToFetch.mapNotNull { badgeId ->
                    try {
                        val badge = badgesCollection.document(badgeId).get().await().toObject(Badge::class.java)
                        badge?.let { badgeCache[badgeId] = it }
                        badge
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Error fetching badge $badgeId: ${e.localizedMessage}")
                        null
                    }
                }
                Log.d("ProfileViewModel", "Fetched ${newBadges.size} new badges from Firestore")
            }

            // Build final badge list from cache
            val allBadges = earnedBadgeIds.mapNotNull { badgeCache[it] }
            _earnedBadges.value = allBadges
            lastFetchedBadgeIds = earnedBadgeIds

        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error fetching badges: ${e.localizedMessage}")
            _earnedBadges.value = emptyList()
        }
    }
    fun startProfileListener() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (!userId.isNullOrEmpty()) {
                profileListenerRegistration?.remove()
                profileListenerRegistration = fireStore.collection("users")
                    .document(userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("ProfileViewModel", "Snapshot listener error: ${error.localizedMessage}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            viewModelScope.launch {
                                val premium = dataStoreManager.isPremium.value
                                val deviceId = dataStoreManager.getOrCreateDeviceId()
                                val deviceSnapshot = fireStore.collection("users")
                                    .document(userId)
                                    .collection("devices")
                                    .document(deviceId)
                                    .get()
                                    .await()
                                val deviceActiveProfileId = deviceSnapshot.getString("activeProfileId")

                                val parentAccount = ParentAccount(
                                    parentName = snapshot.getString("parentName") ?: "",
                                    primaryEmail = snapshot.getString("primaryEmail")
                                        ?: snapshot.getString("email"),
                                    contactEmail = snapshot.getString("contactEmail"),
                                    userId = snapshot.getString("userId") ?: "",
                                    profiles = (snapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()).map { profileMap ->
                                        val profileId = profileMap["profileId"] as? String
                                            ?: java.util.UUID.randomUUID().toString()
                                        ChildProfile(
                                            profileId = profileId,
                                            childName = profileMap["childName"] as? String ?: "",
                                            age = (profileMap["age"] as? Long)?.toInt() ?: 0,
                                            avatar = profileMap["avatar"] as? String ?: "",
                                            xp = (profileMap["xp"] as? Long)?.toInt() ?: 0,
                                            earnedBadges = profileMap["earnedBadges"] as? List<String> ?: emptyList(),
                                            chaptersCompleted = profileMap["chaptersCompleted"] as? List<String> ?: emptyList(),
                                            storiesCompleted = profileMap["storiesCompleted"] as? List<String> ?: emptyList(),
                                            levelsCompleted = profileMap["levelsCompleted"] as? List<String> ?: emptyList(),
                                            quizzesAttended = (profileMap["quizzesAttended"] as? Long)?.toInt() ?: 0,
                                            dayStreak = (profileMap["dayStreak"] as? Long)?.toInt() ?: 0,
                                            lessonsCompleted = profileMap["lessonsCompleted"] as? List<String> ?: emptyList(),
                                            activitiesCompleted = profileMap["activitiesCompleted"] as? List<String> ?: emptyList(),
                                            createdAt = profileMap["createdAt"] as? Long ?: System.currentTimeMillis()
                                        )
                                    },
                                    activeProfileIndex = 0,
                                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                                    lastUpdated = snapshot.getLong("lastUpdated") ?: System.currentTimeMillis()
                                )

                                // Update local state
                                _parentAccountState.value = parentAccount

                                if (parentAccount.profiles.isNotEmpty()) {
                                    val resolvedActiveProfileId = resolveActiveProfileId(
                                        parentAccount = parentAccount,
                                        deviceActiveProfileId = deviceActiveProfileId,
                                        isPremium = premium,
                                        legacyIndex = (snapshot.getLong("activeProfileIndex") ?: 0).toInt()
                                    )
                                    val activeIndex = parentAccount.profiles.indexOfFirst { it.profileId == resolvedActiveProfileId }
                                    val safeIndex = if (activeIndex >= 0) activeIndex else 0
                                    val currentProfile = parentAccount.profiles[safeIndex]

                                    _parentAccountState.value = parentAccount.copy(activeProfileIndex = safeIndex)
                                    _currentChildProfile.value = currentProfile

                                    if (!resolvedActiveProfileId.isNullOrBlank() && resolvedActiveProfileId != deviceActiveProfileId) {
                                        fireStore.collection("users")
                                            .document(userId)
                                            .collection("devices")
                                            .document(deviceId)
                                            .set(
                                                mapOf(
                                                    "activeProfileId" to resolvedActiveProfileId,
                                                    "platform" to "Android",
                                                    "lastSeen" to System.currentTimeMillis()
                                                ),
                                                com.google.firebase.firestore.SetOptions.merge()
                                            )
                                            .await()
                                    }

                                    // Fetch earned badges for the current profile
                                    fetchEarnedBadges(currentProfile.earnedBadges)
                                }

                                _isLoading.value = false
                            }
                        } else {
                            _isLoading.value = false
                            Log.d("ProfileViewModel", "Snapshot is null or user document doesn't exist.")
                        }
                    }
            }
        }
    }

    fun stopProfileListener() {
        profileListenerRegistration?.remove()
        profileListenerRegistration = null
    }

    /**
     * Pre-warm badge cache by fetching all badges
     * This eliminates loading delays when users view badges
     */
    private fun preWarmBadgeCache() {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Pre-warming badge cache...")
                val badgesSnapshot = fireStore.collection("badges").get().await()

                var cachedCount = 0
                badgesSnapshot.documents.forEach { doc ->
                    val badge = doc.toObject(Badge::class.java)
                    badge?.let {
                        badgeCache[it.id] = it
                        cachedCount++
                    }
                }

                Log.d("ProfileViewModel", "Badge cache pre-warmed with $cachedCount badges")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error pre-warming badge cache: ${e.localizedMessage}")
            }
        }
    }


    /**
     * Updates stats for the active profile in Firestore
     */
    suspend fun updateProfileStats(
        userId: String,
        statsUpdate: (MutableMap<String, Any>) -> Unit
    ) {
        try {
            val userRef = fireStore.collection("users").document(userId)
            val documentSnapshot = userRef.get().await()

            if (documentSnapshot.exists()) {
                val currentParent = _parentAccountState.value
                if (currentParent != null && currentParent.profiles.isNotEmpty()) {
                    val activeProfileIndex = currentParent.activeProfileIndex
                    val profiles = documentSnapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                    val updatedProfiles = profiles.toMutableList()
                    
                    if (activeProfileIndex < updatedProfiles.size) {
                        val currentProfile = updatedProfiles[activeProfileIndex].toMutableMap()
                        
                        // Apply the stats update
                        statsUpdate(currentProfile)
                        
                        updatedProfiles[activeProfileIndex] = currentProfile
                        
                        // Update Firestore
                        userRef.update(
                            mapOf(
                                "profiles" to updatedProfiles,
                                "lastUpdated" to System.currentTimeMillis()
                            )
                        ).await()
                        
                        Log.d("ProfileViewModel", "Profile stats updated successfully")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error updating profile stats: ${e.localizedMessage}")
        }
    }

    /**
     * Local-only daily lesson limiter facade for the current child profile.
     *
     * Returns null if no active profile is loaded (in which case callers should allow the lesson).
     */
    suspend fun tryConsumeDailyLessonSlot(): DataStoreManager.DailyLessonResult? {
        val currentProfile = _currentChildProfile.value ?: return null
        val baseLimit = dataStoreManager.getEffectiveDailyLessonLimit()
        val limitedCompleted = getLifetimeLimitedCompletedCount(currentProfile)
        val maxPerDay = dataStoreManager.adjustDailyLimitForProgress(
            baseLimit = baseLimit,
            lifetimeLimitedCompletedCount = limitedCompleted
        )
        return dataStoreManager.tryConsumeDailyLessonSlot(
            profileId = currentProfile.profileId,
            maxPerDay = maxPerDay
        )
    }

    /**
     * Returns current daily lesson status for gating without consuming a slot.
     */
    suspend fun getDailyLessonStatus(): DataStoreManager.DailyLessonResult? {
        val currentProfile = _currentChildProfile.value ?: return null
        val baseLimit = dataStoreManager.getEffectiveDailyLessonLimit()
        val limitedCompleted = getLifetimeLimitedCompletedCount(currentProfile)
        val maxPerDay = dataStoreManager.adjustDailyLimitForProgress(
            baseLimit = baseLimit,
            lifetimeLimitedCompletedCount = limitedCompleted
        )
        return dataStoreManager.getDailyLessonStatus(
            profileId = currentProfile.profileId,
            maxPerDay = maxPerDay
        )
    }

    /**
     * Counts how many limited chapters (lesson + story) this child has completed lifetime.
     * Used to ensure the 5-per-day "reward" only applies while within the first 5.
     */
    private fun getLifetimeLimitedCompletedCount(profile: ChildProfile): Int {
        val lessonIds = profile.lessonsCompleted
        val storyIds = profile.storiesCompleted
        return (lessonIds + storyIds).distinct().size
    }

    /**
     * Updates the active child's name in Firestore and local state.
     */
    fun updateChildName(newName: String) {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (userId.isNullOrEmpty()) return@launch

            try {
                val userRef = fireStore.collection("users").document(userId)
                val documentSnapshot = userRef.get().await()

                if (documentSnapshot.exists()) {
                    val currentParent = _parentAccountState.value
                    if (currentParent != null && currentParent.profiles.isNotEmpty()) {
                        val activeProfileIndex = currentParent.activeProfileIndex
                        val updatedProfiles = currentParent.profiles.toMutableList()
                        if (activeProfileIndex in updatedProfiles.indices) {
                            val currentProfile = updatedProfiles[activeProfileIndex]
                            val updatedProfile = currentProfile.copy(childName = newName)
                            updatedProfiles[activeProfileIndex] = updatedProfile

                            val updatedParent = currentParent.copy(
                                profiles = updatedProfiles,
                                lastUpdated = System.currentTimeMillis()
                            )

                            // Persist to Firestore
                            userRef.set(updatedParent).await()

                            // Update local state
                            _parentAccountState.value = updatedParent
                            _currentChildProfile.value = updatedProfile

                            Log.d("ProfileViewModel", "Child name updated successfully to $newName")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating child name: ${e.localizedMessage}")
                // Fallback to refetch on error
                fetchUserProfile()
            }
        }
    }

    /**
     * Updates the child's avatar in Firestore, then refreshes the user profile.
     */
    fun updateAvatar(newAvatarName: String) {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (!userId.isNullOrEmpty()) {
                try {
                    val userRef = fireStore.collection("users").document(userId)
                    val documentSnapshot = userRef.get().await()

                    if (documentSnapshot.exists()) {
                        // Get current parent account and update active child profile
                        val currentParent = _parentAccountState.value
                        if (currentParent != null && currentParent.profiles.isNotEmpty()) {
                            // Update the active child profile's avatar
                            val activeProfileIndex = currentParent.activeProfileIndex
                            val updatedProfiles = currentParent.profiles.toMutableList()
                            val currentProfile = updatedProfiles[activeProfileIndex]
                            
                            // Create updated child profile with new avatar
                            val updatedProfile = currentProfile.copy(avatar = newAvatarName)
                            updatedProfiles[activeProfileIndex] = updatedProfile
                            
                            // Create updated parent account
                            val updatedParent = currentParent.copy(
                                profiles = updatedProfiles,
                                lastUpdated = System.currentTimeMillis()
                            )
                            
                            // Update Firestore with profiles only
                            userRef.update(
                                "profiles",
                                updatedProfiles.map { it.toMap() },
                                "lastUpdated",
                                System.currentTimeMillis()
                            ).await()
                            
                            // Update local state
                            _parentAccountState.value = updatedParent
                            _currentChildProfile.value = updatedProfile
                            
                            Log.d("ProfileViewModel", "Avatar updated successfully to $newAvatarName for profile: ${updatedProfile.childName}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error updating avatar: ${e.localizedMessage}")
                    // Re-fetch to ensure state consistency on error
                    fetchUserProfile()
                }
            }
        }
    }

    private fun resolveActiveProfileId(
        parentAccount: ParentAccount,
        deviceActiveProfileId: String?,
        isPremium: Boolean,
        legacyIndex: Int
    ): String? {
        val profiles = parentAccount.profiles
        if (profiles.isEmpty()) return null

        val legacySafeIndex = legacyIndex.coerceIn(0, profiles.lastIndex)
        var desiredId = deviceActiveProfileId
        if (desiredId.isNullOrBlank() || profiles.none { it.profileId == desiredId }) {
            desiredId = profiles[legacySafeIndex].profileId
        }

        val desiredIndex = profiles.indexOfFirst { it.profileId == desiredId }
        return if (!isPremium && desiredIndex > 0) {
            profiles.first().profileId
        } else {
            desiredId
        }
    }

    private fun ChildProfile.toMap(): Map<String, Any> {
        return mapOf(
            "profileId" to profileId,
            "childName" to childName,
            "age" to age,
            "avatar" to avatar,
            "xp" to xp,
            "earnedBadges" to earnedBadges,
            "chaptersCompleted" to chaptersCompleted,
            "storiesCompleted" to storiesCompleted,
            "levelsCompleted" to levelsCompleted,
            "quizzesAttended" to quizzesAttended,
            "dayStreak" to dayStreak,
            "lessonsCompleted" to lessonsCompleted,
            "activitiesCompleted" to activitiesCompleted,
            "createdAt" to createdAt
        )
    }
}
