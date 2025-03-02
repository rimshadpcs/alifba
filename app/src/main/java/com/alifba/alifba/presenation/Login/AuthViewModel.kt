package com.alifba.alifba.presenation.Login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.features.authentication.OnboardingDataStoreManager
import com.alifba.alifba.features.authentication.usecase.SignInUseCase
import com.alifba.alifba.features.authentication.usecase.SignUpUseCase
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

    // --------------------------------------------------
    // STATE AND DATA FLOWS
    // --------------------------------------------------

    val email: StateFlow<String?> = dataStoreManager.email
    val password: StateFlow<String?> = dataStoreManager.password
    val userId: StateFlow<String?> = dataStoreManager.userId

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> get() = _authState

    private val _userProfileState = MutableStateFlow<UserProfile?>(null)
    val userProfileState: StateFlow<UserProfile?> get() = _userProfileState

    private val _profileCreationState = MutableStateFlow<ProfileCreationState>(ProfileCreationState.Idle)
    val profileCreationState: StateFlow<ProfileCreationState> get() = _profileCreationState

    val hasCompletedOnboarding: Flow<Boolean> = onboardingDataStoreManager.hasCompletedOnboarding

    // --------------------------------------------------
    // SIGN UP
    // --------------------------------------------------

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

    // --------------------------------------------------
    // SIGN IN
    // --------------------------------------------------

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

    // --------------------------------------------------
    // CHECK FOR CHILD PROFILES
    // --------------------------------------------------

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
                val childProfiles = docSnapshot.get("childProfiles") as? List<*>
                val hasProfiles = childProfiles?.isNotEmpty() == true
                Log.d("AuthViewModel", "Has child profiles? $hasProfiles")
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

    // --------------------------------------------------
    // CREATE/UPDATE PROFILE DATA
    // --------------------------------------------------

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

        // 2) Build user data
        val childProfile = mapOf(
            "childName" to childName,
            "age" to selectedAge,
            "avatar" to selectedAvatarName
        )

        val userData = mapOf(
            "parentName" to parentName,
            "email" to email,
            "userId" to uid,
            "xp" to 0,
            "chapters_completed" to emptyList<String>(),
            "stories_completed" to emptyList<String>(),
            "levels_completed" to emptyList<String>(),
            "earned_badges" to emptyList<String>(),
            "quizzes_attended" to 0,
            "streak" to 0,
            "childProfiles" to listOf(childProfile)
        )

        // 3) Write data directly (no transaction needed)
        firestore.collection("users")
            .document(uid)
            .set(userData)
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

    // --------------------------------------------------
    // TIMEZONE HANDLING (OPTIONAL)
    // --------------------------------------------------

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

    // --------------------------------------------------
    // FETCH USER PROFILE
    // --------------------------------------------------

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

                val parentName = docSnap.getString("parentName") ?: "N/A"
                val email = docSnap.getString("email") ?: "N/A"
                val userId = docSnap.getString("userId") ?: "N/A"

                val childProfiles = docSnap.get("childProfiles") as? List<Map<String, Any>> ?: emptyList()
                val xp = (docSnap.getLong("xp") ?: 0).toInt()
                val chaptersCompleted = docSnap.get("chapters_completed") as? List<String> ?: emptyList()
                val storiesCompleted = docSnap.get("stories_completed") as? List<String> ?: emptyList()
                val levelsCompleted = docSnap.get("levels_completed") as? List<String> ?: emptyList()
                val activitiesCompleted = docSnap.get("activities_completed") as? List<String> ?: emptyList()
                val lessonsCompleted = docSnap.get("lessons_completed") as? List<String> ?: emptyList()

                if (childProfiles.isNotEmpty()) {
                    val childProfile = childProfiles[0]
                    val childName = childProfile["childName"] as? String ?: "N/A"
                    val age = (childProfile["age"] as? Long)?.toInt() ?: 0
                    val avatar = childProfile["avatar"] as? String ?: "N/A"

                    _userProfileState.value = UserProfile(
                        parentName = parentName,
                        childName = childName,
                        age = age,
                        avatar = avatar,
                        email = email,
                        userId = userId,
                        xp = xp,
                        chaptersCompleted = chaptersCompleted,
                        storiesCompleted = storiesCompleted,
                        levelsCompleted = levelsCompleted,
                        lessonsCompleted = lessonsCompleted,
                        activitiesCompleted = activitiesCompleted
                    )
                } else {
                    // No childProfiles means we just have parent data
                    _userProfileState.value = UserProfile(
                        parentName = parentName,
                        childName = "",
                        age = 0,
                        avatar = "",
                        email = email,
                        userId = userId,
                        xp = xp,
                        chaptersCompleted = chaptersCompleted,
                        storiesCompleted = storiesCompleted,
                        levelsCompleted = levelsCompleted,
                        lessonsCompleted = lessonsCompleted,
                        activitiesCompleted = activitiesCompleted
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user profile: ${e.localizedMessage}")
            }
        }
    }

    // --------------------------------------------------
    // LOGOUT
    // --------------------------------------------------

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            dataStoreManager.clearUserDetails()
            _userProfileState.value = null
        }
    }

    // --------------------------------------------------
    // EMAIL VERIFICATION
    // --------------------------------------------------

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

    // --------------------------------------------------
    // DELETE USER ACCOUNT
    // --------------------------------------------------

    fun deleteUserAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: throw Exception("No user is currently logged in")
                val uid = currentUser.uid

                // Delete any subcollections first
                deleteUserSubcollections(uid)

                // Then delete the user's document in Firestore
                firestore.collection("users").document(uid).delete().await()

                // Finally delete the user from Firebase Auth
                currentUser.delete().await()

                // Clear local DataStore if needed
                dataStoreManager.clearUserDetails()

                onSuccess()
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

    // --------------------------------------------------
    // INTERNAL HELPERS
    // --------------------------------------------------

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
}


// --------------------------------------------------
// HELPER DATA CLASSES
// --------------------------------------------------

data class UserProfile(
    val parentName: String,
    val childName: String,
    val age: Int,
    val avatar: String,
    val email: String,
    val userId: String,
    val xp: Int = 0,
    val earnedBadges: List<String> = emptyList(),
    val chaptersCompleted: List<String> = emptyList(),
    val storiesCompleted: List<String> = emptyList(),
    val levelsCompleted: List<String> = emptyList(),
    val quizzesAttended: Int = 0,
    val dayStreak: Int = 0,
    val lessonsCompleted: List<String> = emptyList(),
    val activitiesCompleted: List<String> = emptyList(),
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
