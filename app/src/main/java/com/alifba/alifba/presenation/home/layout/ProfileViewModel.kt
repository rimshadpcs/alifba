package com.alifba.alifba.presenation.home.layout

import android.util.Log
import androidx.compose.material.Badge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.presenation.Login.ProfileCreationState
import com.alifba.alifba.presenation.Login.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
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

    private val _userProfileState = MutableStateFlow<UserProfile?>(null)
    val userProfileState: StateFlow<UserProfile?> get() = _userProfileState

    /**
     * Fetches all relevant user data (XP, chaptersCompleted, badges, child profile, etc.)
     * from Firestore and updates the _userProfileState.
     */
    fun fetchUserProfile() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (!userId.isNullOrEmpty()) {
                try {
                    val documentSnapshot = fireStore.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    if (documentSnapshot.exists()) {
                        // Basic user fields
                        val parentName = documentSnapshot.getString("parentName") ?: ""
                        val email = documentSnapshot.getString("email") ?: ""
                        val userIdFromDoc = documentSnapshot.getString("userId") ?: ""

                        // Child profile
                        val childProfiles = documentSnapshot.get("childProfiles") as? List<Map<String, Any>> ?: emptyList()
                        val childProfile = childProfiles.firstOrNull() // Assuming only one child profile
                        val childName = childProfile?.get("childName") as? String ?: ""
                        val age = (childProfile?.get("age") as? Long)?.toInt() ?: 0
                        val avatar = childProfile?.get("avatar") as? String ?: ""

                        // Stats
                        val xp = (documentSnapshot.getLong("xp") ?: 0).toInt()
                        val chaptersCompleted = documentSnapshot.get("chapters_completed") as? List<String> ?: emptyList()
                        val quizzesAttended = (documentSnapshot.getLong("quizzes_attended") ?: 0).toInt()
                        val dayStreak = (documentSnapshot.getLong("day_streak") ?: 0).toInt()

                        // Update local state
                        _userProfileState.value = UserProfile(
                            parentName = parentName,
                            childName = childName,
                            age = age,
                            avatar = avatar,
                            email = email,
                            userId = userIdFromDoc,
                            xp = xp,
                            quizzesAttended = quizzesAttended,
                            dayStreak = dayStreak,
                            chaptersCompleted = chaptersCompleted
                        )
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

    fun startProfileListener() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (!userId.isNullOrEmpty()) {
                fireStore.collection("users")
                    .document(userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("ProfileViewModel", "Snapshot listener error: ${error.localizedMessage}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            // Fetch fields from the snapshot
                            val parentName = snapshot.getString("parentName") ?: ""
                            val email = snapshot.getString("email") ?: ""
                            val userIdFromDoc = snapshot.getString("userId") ?: ""

                            val childProfiles = snapshot.get("childProfiles") as? List<Map<String, Any>> ?: emptyList()
                            val childProfile = childProfiles.firstOrNull()
                            val childName = childProfile?.get("childName") as? String ?: ""
                            val age = (childProfile?.get("age") as? Long)?.toInt() ?: 0
                            val avatar = childProfile?.get("avatar") as? String ?: ""

                            val xp = (snapshot.getLong("xp") ?: 0).toInt()
                            val chaptersCompleted = snapshot.get("chapters_completed") as? List<String> ?: emptyList()
                            val quizzesAttended = (snapshot.getLong("quizzes_attended") ?: 0).toInt()
                            val dayStreak = (snapshot.getLong("day_streak") ?: 0).toInt()

                            // Update local state
                            _userProfileState.value = UserProfile(
                                parentName = parentName,
                                childName = childName,
                                age = age,
                                avatar = avatar,
                                email = email,
                                userId = userIdFromDoc,
                                xp = xp,
                                quizzesAttended = quizzesAttended,
                                dayStreak = dayStreak,
                                chaptersCompleted = chaptersCompleted
                            )
                        } else {
                            Log.d("ProfileViewModel", "Snapshot is null or user document doesn't exist.")
                        }
                    }
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
                        // Grab existing childProfiles and update the first one
                        val childProfiles = documentSnapshot.get("childProfiles") as? List<Map<String, Any>> ?: emptyList()
                        if (childProfiles.isNotEmpty()) {
                            val updatedChildProfiles = childProfiles.toMutableList()
                            val updatedChildProfile = updatedChildProfiles[0].toMutableMap()

                            updatedChildProfile["avatar"] = newAvatarName
                            updatedChildProfiles[0] = updatedChildProfile

                            // Commit the update
                            userRef.update("childProfiles", updatedChildProfiles).await()
                            Log.d("ProfileViewModel", "Avatar updated successfully to $newAvatarName")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error updating avatar: ${e.localizedMessage}")
                } finally {
                    // Always re-fetch to update local state
                    fetchUserProfile()
                }
            }
        }
    }

}
