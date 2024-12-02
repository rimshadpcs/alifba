package com.alifba.alifba.presenation.home.layout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                        val parentName = documentSnapshot.getString("parentName") ?: ""
                        val childProfiles = documentSnapshot.get("childProfiles") as? List<Map<String, Any>> ?: emptyList()

                        if (childProfiles.isNotEmpty()) {
                            val childProfile = childProfiles[0] // Assuming one child profile for simplicity
                            val childName = childProfile["childName"] as? String ?: ""
                            val age = (childProfile["age"] as? Long)?.toInt() ?: 0
                            val avatar = childProfile["avatar"] as? String ?: ""

                            _userProfileState.value = UserProfile(parentName, childName, age, avatar)
                        }
                    } else {
                        Log.d("ChildProfileViewModel", "User document does not exist")
                    }
                } catch (e: Exception) {
                    Log.e("ChildProfileViewModel", "Error fetching user profile: ${e.localizedMessage}")
                }
            }
        }
    }

    fun updateAvatar(newAvatarName: String) {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            if (!userId.isNullOrEmpty()) {
                try {
                    val userRef = fireStore.collection("users").document(userId)
                    val documentSnapshot = userRef.get().await()

                    if (documentSnapshot.exists()) {
                        val childProfiles = documentSnapshot.get("childProfiles") as? List<Map<String, Any>> ?: emptyList()
                        if (childProfiles.isNotEmpty()) {
                            val updatedChildProfiles = childProfiles.toMutableList()
                            val updatedChildProfile = updatedChildProfiles[0].toMutableMap()
                            updatedChildProfile["avatar"] = newAvatarName
                            updatedChildProfiles[0] = updatedChildProfile

                            userRef.update("childProfiles", updatedChildProfiles)
                            Log.d("ChildProfileViewModel", "Avatar updated successfully to $newAvatarName")
                            fetchUserProfile()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChildProfileViewModel", "Error updating avatar: ${e.localizedMessage}")
                }
            }
        }
    }
}