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
                        // Load multi-profile structure - ONLY from profiles array
                        val profiles = documentSnapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()
                        val parentAccount = ParentAccount(
                            parentName = documentSnapshot.getString("parentName") ?: "",
                            email = documentSnapshot.getString("email") ?: "",
                            userId = documentSnapshot.getString("userId") ?: "",
                            profiles = profiles.map { profileMap ->
                                ChildProfile(
                                    profileId = profileMap["profileId"] as? String ?: java.util.UUID.randomUUID().toString(),
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
                            activeProfileIndex = (documentSnapshot.getLong("activeProfileIndex") ?: 0).toInt(),
                            createdAt = documentSnapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                            lastUpdated = documentSnapshot.getLong("lastUpdated") ?: System.currentTimeMillis()
                        )

                        // Update local state
                        _parentAccountState.value = parentAccount

                        // Set the current active child profile and fetch badges
                        if (parentAccount.profiles.isNotEmpty() && parentAccount.activeProfileIndex < parentAccount.profiles.size) {
                            val currentProfile = parentAccount.profiles[parentAccount.activeProfileIndex]
                            _currentChildProfile.value = currentProfile

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
                            // Load multi-profile structure from real-time updates
                            val parentAccount = ParentAccount(
                                parentName = snapshot.getString("parentName") ?: "",
                                email = snapshot.getString("email") ?: "",
                                userId = snapshot.getString("userId") ?: "",
                                profiles = (snapshot.get("profiles") as? List<Map<String, Any>> ?: emptyList()).map { profileMap ->
                                    ChildProfile(
                                        profileId = profileMap["profileId"] as? String ?: java.util.UUID.randomUUID().toString(),
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
                                activeProfileIndex = (snapshot.getLong("activeProfileIndex") ?: 0).toInt(),
                                createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                                lastUpdated = snapshot.getLong("lastUpdated") ?: System.currentTimeMillis()
                            )

                            // Update local state
                            _parentAccountState.value = parentAccount

                            // Set the current active child profile
                            if (parentAccount.profiles.isNotEmpty() && parentAccount.activeProfileIndex < parentAccount.profiles.size) {
                                val currentProfile = parentAccount.profiles[parentAccount.activeProfileIndex]
                                _currentChildProfile.value = currentProfile
                                
                                // Fetch earned badges for the current profile
                                viewModelScope.launch {
                                    fetchEarnedBadges(currentProfile.earnedBadges)
                                }
                            }

                            _isLoading.value = false
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
                            
                            // Update Firestore with complete parent account
                            userRef.set(updatedParent).await()
                            
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
}

