package com.alifba.alifba.presenation.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.features.authentication.OnboardingDataStoreManager
import com.alifba.alifba.features.authentication.usecase.SignInUseCase
import com.alifba.alifba.features.authentication.usecase.SignUpUseCase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val onboardingDataStoreManager: OnboardingDataStoreManager,
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    val dataStoreManager: DataStoreManager,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    val email: StateFlow<String?> = dataStoreManager.email
    val password: StateFlow<String?> = dataStoreManager.password
    val userId: StateFlow<String?> = dataStoreManager.userId

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> get() = _authState

    // Multi-profile states
    private val _parentAccountState = MutableStateFlow<ParentAccount?>(null)
    val parentAccountState: StateFlow<ParentAccount?> get() = _parentAccountState
    
    private val _currentChildProfile = MutableStateFlow<ChildProfile?>(null)
    val currentChildProfile: StateFlow<ChildProfile?> get() = _currentChildProfile

    private val _profileCreationState = MutableStateFlow<ProfileCreationState>(ProfileCreationState.Idle)
    val profileCreationState: StateFlow<ProfileCreationState> get() = _profileCreationState

    val hasCompletedOnboarding: Flow<Boolean> = onboardingDataStoreManager.hasCompletedOnboarding


    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = signUpUseCase(email, password)
            if (result.isSuccess) {
                // Store email/password


                dataStoreManager.saveUserDetails(email, password, userId = "")

                // Send verification email
                sendEmailVerification(
                    onSuccess = {
                        viewModelScope.launch {
                            onboardingDataStoreManager.setOnboardingCompleted(false)
                            _authState.value = AuthState.Success

                            onSuccess()
                        }
                    },
                    onError = { errorMessage ->
                        _authState.value = AuthState.Error(errorMessage)
                        onError(errorMessage)
                    }
                )
            } else {
                val msg = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                _authState.value = AuthState.Error(msg)
                onError(msg)
            }
        }
    }

    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = signInUseCase(email, password)
            if (result.isSuccess) {
                val currentUser = auth.currentUser
                if (currentUser?.isEmailVerified == true) {
                    // Email is verified, proceed with login
                    fetchUserData(email, password, onResult, onError)
                } else {
                    _authState.value = AuthState.EmailNotVerified
                    onError("Please verify your email before logging in")
                }
            } else {
                val msg = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                _authState.value = AuthState.Error(msg)
                onError(msg)
            }
        }
    }

    private fun fetchUserData(email: String, password: String, onResult: (Boolean) -> Unit, onError: (String) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDoc = querySnapshot.documents[0]
                    val foundUserId = userDoc.getString("userId")
                    if (foundUserId != null) {
                        viewModelScope.launch {
                            dataStoreManager.saveUserDetails(email, password, foundUserId)
                            fetchAndSaveFcmToken(foundUserId)
                            
                            // Set onboarding as completed for existing users during signin
                            onboardingDataStoreManager.setOnboardingCompleted(true)
                            
                            val hasProfiles = checkForChildProfiles()
                            _authState.value = AuthState.Success
                            onResult(hasProfiles)
                        }
                    } else {
                        _authState.value = AuthState.Error("User ID not found.")
                        onError("User ID not found.")
                    }
                } else {
                    _authState.value = AuthState.Error("User not found.")
                    onError("User not found.")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Unknown error")
                onError(e.localizedMessage ?: "Unknown error")
            }
    }

    suspend fun checkForChildProfiles(): Boolean {
        val currentUid = dataStoreManager.userId.first()
        Log.d("AuthViewModel", "checkForChildProfiles: userId = $currentUid")

        if (currentUid.isNullOrEmpty()) {
            Log.d("AuthViewModel", "No userId in DataStore")
            return false
        }

        return try {
            val docSnapshot = firestore.collection("users")
                .document(currentUid)
                .get()
                .await()

            if (docSnapshot.exists()) {
                val profiles = docSnapshot.get("profiles") as? List<*>
                val hasProfiles = profiles?.isNotEmpty() == true
                Log.d("AuthViewModel", "Has child profiles? $hasProfiles (${profiles?.size ?: 0} profiles)")
                hasProfiles
            } else {
                Log.d("AuthViewModel", "User document does not exist")
                false
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching user doc: ${e.localizedMessage}")
            false
        }
    }

    fun sendProfileDataToFireStore(
        parentName: String,
        childName: String,
        selectedAge: Int?,
        selectedAvatarName: String
    ) {
        if (_profileCreationState.value is ProfileCreationState.Success) {
            // Already created, ignore to prevent duplicates
            Log.d("Firestore", "Profile already created, ignoring duplicate click")
            return
        }
        _profileCreationState.value = ProfileCreationState.Idle

        // 1) Get the current user
        val currentUser = auth.currentUser ?: run {
            _profileCreationState.value = ProfileCreationState.Error("No logged-in user")
            return
        }
        val uid = currentUser.uid
        val email = currentUser.email ?: "N/A"

        // 2) Build new multi-profile structure
        val childProfile = ChildProfile(
            childName = childName,
            age = selectedAge ?: 0,
            avatar = selectedAvatarName
        )

        val parentAccount = ParentAccount(
            parentName = parentName,
            email = email,
            userId = uid,
            profiles = listOf(childProfile),
            activeProfileIndex = 0
        )

        // 3) Write data directly (no transaction needed)
        firestore.collection("users")
            .document(uid)
            .set(parentAccount)
            .addOnSuccessListener {
                viewModelScope.launch {
                    // Save details in DataStore
                    dataStoreManager.saveUserDetails(
                        email = email,
                        password = "",
                        userId = uid
                    )
                }
               fetchAndSaveFcmToken(uid)
                _profileCreationState.value = ProfileCreationState.Success
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Failed to set user data.", e)
                _profileCreationState.value = ProfileCreationState.Error(e.localizedMessage ?: "Unknown error")
            }
    }

    suspend fun updateTimeZoneIfNeeded(context: Context, userId: String) {
        val currentTimeZone = TimeZone.getDefault().id
        val storedTimeZone = dataStoreManager.getTimeZone(context)

        if (storedTimeZone == currentTimeZone) {
            Log.d("TimeZone", "Time zone unchanged: $currentTimeZone")
            return
        }

        dataStoreManager.saveTimeZone(context, currentTimeZone)
        firestore.collection("users")
            .document(userId)
            .update("timeZone", currentTimeZone)
            .addOnSuccessListener {
                Log.d("TimeZone", "User time zone updated to $currentTimeZone")
            }
            .addOnFailureListener { e ->
                Log.e("TimeZone", "Error updating time zone: ${e.localizedMessage}")
            }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val currentUid = dataStoreManager.userId.first()
            if (currentUid.isNullOrEmpty()) {
                Log.d("AuthViewModel", "No userId found in DataStore.")
                return@launch
            }

            try {
                val docSnap = firestore.collection("users")
                    .document(currentUid)
                    .get()
                    .await()

                if (!docSnap.exists()) {
                    Log.d("AuthViewModel", "User document does not exist.")
                    return@launch
                }

                // Load multi-profile structure
                loadMultiProfileData(docSnap)

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user profile: ${e.localizedMessage}")
            }
        }
    }

    private fun loadMultiProfileData(docSnap: com.google.firebase.firestore.DocumentSnapshot) {
        try {
            val parentAccount = ParentAccount(
                parentName = docSnap.getString("parentName") ?: "",
                email = docSnap.getString("email") ?: "",
                userId = docSnap.getString("userId") ?: "",
                profiles = (docSnap.get("profiles") as? List<Map<String, Any>> ?: emptyList()).map { profileMap ->
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
                activeProfileIndex = (docSnap.getLong("activeProfileIndex") ?: 0).toInt(),
                createdAt = docSnap.getLong("createdAt") ?: System.currentTimeMillis(),
                lastUpdated = docSnap.getLong("lastUpdated") ?: System.currentTimeMillis()
            )

            _parentAccountState.value = parentAccount

            // Set the current active child profile
            if (parentAccount.profiles.isNotEmpty() && parentAccount.activeProfileIndex < parentAccount.profiles.size) {
                _currentChildProfile.value = parentAccount.profiles[parentAccount.activeProfileIndex]
            }

            Log.d("AuthViewModel", "Loaded parent account with ${parentAccount.profiles.size} profiles")

        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error loading multi-profile data: ${e.message}")
        }
    }


    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            dataStoreManager.clearUserDetails()
            _parentAccountState.value = null
            _currentChildProfile.value = null
        }
    }

    fun resendVerificationEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            sendEmailVerification(
                onSuccess = {
                    // Use viewModelScope to call the suspend function
                    viewModelScope.launch {
                        onboardingDataStoreManager.setOnboardingCompleted(false)
                    }
                    onSuccess()
                },
                onError = { errorMessage ->
                    onError(errorMessage)
                }
            )
        } else {
            onError("No user is signed in")
        }
    }


    fun checkEmailVerified(onVerified: () -> Unit, onNotVerified: () -> Unit) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { reloadTask ->
            if (reloadTask.isSuccessful) {
                val refreshedUser = auth.currentUser
                if (refreshedUser?.isEmailVerified == true) {
                    onVerified()
                } else {
                    onNotVerified()
                }
            } else {
                onNotVerified()
            }
        }
    }

    fun deleteUserAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        userPassword: String
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: throw Exception("No user is currently logged in")
                val uid = currentUser.uid

                // Reauthenticate the user using the password provided.
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, userPassword)
                currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Deletion logic now runs entirely within a viewModelScope
                        viewModelScope.launch {
                            try {
                                // First, delete any subcollections and the main user document from Firestore.
                                deleteUserSubcollections(uid)
                                firestore.collection("users").document(uid).delete().await()

                                // After Firestore data is gone, delete the Firebase Auth user.
                                currentUser.delete().await()

                                // Clear local data and call onSuccess.
                                dataStoreManager.clearUserDetails()
                                onSuccess()
                            } catch (e: Exception) {
                                onError(e.localizedMessage ?: "An error occurred during deletion.")
                            }
                        }
                    } else {
                        onError(reauthTask.exception?.localizedMessage ?: "Reauthentication failed")
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    private suspend fun deleteUserSubcollections(userId: String) {
        // Example code: if you have a "children" subcollection or "progress" subcollection, delete them
        val userRef = firestore.collection("users").document(userId)

        val childrenSnap = userRef.collection("children").get().await()
        for (doc in childrenSnap.documents) {
            doc.reference.delete().await()
        }

        val progressSnap = userRef.collection("progress").get().await()
        for (doc in progressSnap.documents) {
            doc.reference.delete().await()
        }
    }

    private fun sendEmailVerification(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.localizedMessage ?: "Failed to send verification email")
                }
            }
    }

    fun fetchAndSaveFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "Fetching FCM token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result ?: return@addOnCompleteListener
                Log.d("FCM", "Fetched Token: $token")

                // Save token to Firestore
                firestore.collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("FCM", "FCM token updated in Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Error: ${e.localizedMessage}")
                    }
            }
    }


    fun addNewChildProfile(childName: String, age: Int, avatar: String) {
        viewModelScope.launch {
            try {
                val currentParent = _parentAccountState.value
                if (currentParent == null) {
                    Log.e("Profile", "No parent account found")
                    return@launch
                }

                val newChildProfile = ChildProfile(
                    childName = childName,
                    age = age,
                    avatar = avatar
                )

                val updatedParent = currentParent.copy(
                    profiles = currentParent.profiles + newChildProfile,
                    lastUpdated = System.currentTimeMillis()
                )

                // Update Firestore
                firestore.collection("users").document(currentParent.userId)
                    .set(updatedParent).await()

                // Update local state
                _parentAccountState.value = updatedParent
                Log.d("Profile", "Added new child profile: ${childName}")
                
            } catch (e: Exception) {
                Log.e("Profile", "Failed to add child profile: ${e.message}")
            }
        }
    }

    fun switchToChildProfile(profileIndex: Int) {
        viewModelScope.launch {
            val currentParent = _parentAccountState.value
            if (currentParent == null || profileIndex >= currentParent.profiles.size) {
                Log.e("Profile", "Invalid profile index or no parent account")
                return@launch
            }

            val updatedParent = currentParent.copy(
                activeProfileIndex = profileIndex,
                lastUpdated = System.currentTimeMillis()
            )

            try {
                // Update Firestore
                firestore.collection("users").document(currentParent.userId)
                    .update("activeProfileIndex", profileIndex, "lastUpdated", System.currentTimeMillis()).await()

                // Update local state
                _parentAccountState.value = updatedParent
                _currentChildProfile.value = updatedParent.profiles[profileIndex]
                Log.d("Profile", "Switched to profile: ${updatedParent.profiles[profileIndex].childName}")
                
            } catch (e: Exception) {
                Log.e("Profile", "Failed to switch profile: ${e.message}")
            }
        }
    }

    fun removeChildProfile(profileIndex: Int) {
        viewModelScope.launch {
            try {
                val currentParent = _parentAccountState.value
                if (currentParent == null || profileIndex >= currentParent.profiles.size) {
                    Log.e("Profile", "Invalid profile index or no parent account")
                    return@launch
                }

                // Allow removing even if it's the last profile

                val updatedProfiles = currentParent.profiles.toMutableList()
                val removedProfile = updatedProfiles.removeAt(profileIndex)

                // Handle empty profiles list
                val newActiveIndex = if (updatedProfiles.isEmpty()) {
                    0
                } else {
                    when {
                        currentParent.activeProfileIndex == profileIndex -> 0 // Switch to first profile
                        currentParent.activeProfileIndex > profileIndex -> currentParent.activeProfileIndex - 1
                        else -> currentParent.activeProfileIndex
                    }
                }

                val updatedParent = currentParent.copy(
                    profiles = updatedProfiles,
                    activeProfileIndex = newActiveIndex,
                    lastUpdated = System.currentTimeMillis()
                )

                // Update Firestore
                firestore.collection("users").document(currentParent.userId)
                    .set(updatedParent).await()

                // Update local state
                _parentAccountState.value = updatedParent
                _currentChildProfile.value = if (updatedProfiles.isNotEmpty()) {
                    updatedProfiles[newActiveIndex]
                } else {
                    null
                }
                Log.d("Profile", "Removed profile: ${removedProfile.childName}")
                
            } catch (e: Exception) {
                Log.e("Profile", "Failed to remove profile: ${e.message}")
            }
        }
    }
}


// Multi-profile data structure
data class ParentAccount(
    val parentName: String,
    val email: String,
    val userId: String,
    val profiles: List<ChildProfile> = emptyList(),
    val activeProfileIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class ChildProfile(
    val profileId: String = java.util.UUID.randomUUID().toString(),
    val childName: String,
    val age: Int,
    val avatar: String,
    val xp: Int = 0,
    val earnedBadges: List<String> = emptyList(),
    val chaptersCompleted: List<String> = emptyList(),
    val storiesCompleted: List<String> = emptyList(),
    val levelsCompleted: List<String> = emptyList(),
    val quizzesAttended: Int = 0,
    val dayStreak: Int = 0,
    val lessonsCompleted: List<String> = emptyList(),
    val activitiesCompleted: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// For profile creation or error states
sealed class ProfileCreationState {
    object Idle : ProfileCreationState()
    object Success : ProfileCreationState()
    data class Error(val message: String) : ProfileCreationState()
}

// For authentication states
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object EmailNotVerified : AuthState()
    data class Error(val message: String) : AuthState()
    object NeedsProfile : AuthState()
}
