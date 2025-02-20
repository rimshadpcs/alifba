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
import com.google.firebase.firestore.DocumentReference
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
) : ViewModel() {

    val email: StateFlow<String?> = dataStoreManager.email
    val password: StateFlow<String?> = dataStoreManager.password
    val userId: StateFlow<String?> = dataStoreManager.userId
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _profileCreationState = MutableStateFlow<ProfileCreationState>(ProfileCreationState.Idle)
    val profileCreationState: StateFlow<ProfileCreationState> get() = _profileCreationState

    private val _userProfileState = MutableStateFlow<UserProfile?>(null)
    val userProfileState: StateFlow<UserProfile?> get() = _userProfileState
    val authState: StateFlow<AuthState> get() = _authState

    val hasCompletedOnboarding: Flow<Boolean> = onboardingDataStoreManager.hasCompletedOnboarding

    suspend fun setOnboardingCompleted(completed: Boolean) {
        onboardingDataStoreManager.setOnboardingCompleted(completed)
    }
    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = signUpUseCase(email, password)
            if (result.isSuccess) {
                // Store just email/password for now (or skip storing password if not needed)
                dataStoreManager.saveUserDetails(email, password, userId = "")

                // Mark user as partially set up, or proceed to next step...
                onboardingDataStoreManager.setOnboardingCompleted(false)
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }


    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = signInUseCase(email, password)
            if (result.isSuccess) {
                val db = FirebaseFirestore.getInstance()
                try {
                    val querySnapshot = db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val userDoc = querySnapshot.documents[0]
                        val userId = userDoc.getString("userId")
                        if (userId != null) {
                            dataStoreManager.saveUserDetails(email, password, userId)
                            fetchAndSaveFcmToken(userId)
                            val hasProfiles = checkForChildProfiles()
                            onResult(hasProfiles)
                        }
                        else {
                            onError("User ID not found.")
                        }
                    } else {
                        onError("User not found.")
                    }
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Unknown error")
                }
            } else {
                onError(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }



    suspend fun checkForChildProfiles(): Boolean {
        val userId = dataStoreManager.userId.first()
        Log.d("AuthViewModel", "checkForChildProfiles: userId = $userId")

        if (!userId.isNullOrEmpty()) {
            try {
                val documentSnapshot = fireStore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                Log.d("AuthViewModel", "Doc exists? ${documentSnapshot.exists()} childProfiles? ${documentSnapshot.get("childProfiles")}")
                if (documentSnapshot.exists()) {
                    val childProfiles = documentSnapshot.get("childProfiles") as? List<*>
                    val hasProfiles = childProfiles != null && childProfiles.isNotEmpty()
                    Log.d("AuthViewModel", "checkForChildProfiles: Has profiles: $hasProfiles")
                    return hasProfiles
                } else {
                    Log.d("AuthViewModel", "checkForChildProfiles: User document does not exist")
                    return false
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user document: ${e.localizedMessage}")
                return false
            }
        } else {
            Log.d("AuthViewModel", "checkForChildProfiles: userId is null or empty")
            return false
        }
    }

    fun sendProfileDataToFireStore(
        parentName: String,
        childName: String,
        selectedAge: Int?,
        selectedAvatarName: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return

        if (_profileCreationState.value is ProfileCreationState.Success) {
            Log.d("Firestore", "Profile already created, ignoring duplicate click")
            return
        }

        _profileCreationState.value = ProfileCreationState.Idle  // Track progress

        val settingsRef = db.collection("settings").document("userCounter")

        settingsRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                settingsRef.set(mapOf("currentId" to 10000L)).addOnSuccessListener {
                    proceedWithTransaction(
                        db, settingsRef, email, parentName, childName, selectedAge, selectedAvatarName
                    )
                }
            } else {
                proceedWithTransaction(
                    db, settingsRef, email, parentName, childName, selectedAge, selectedAvatarName
                )
            }
        }.addOnFailureListener { e ->
            _profileCreationState.value = ProfileCreationState.Error(e.localizedMessage ?: "Unknown error")
        }
    }


    private fun proceedWithTransaction(
        db: FirebaseFirestore,
        settingsRef: DocumentReference,
        email: String,
        parentName: String,
        childName: String,
        selectedAge: Int?,
        selectedAvatarName: String
    ) {
        db.runTransaction { transaction ->
            val currentId = transaction.get(settingsRef).getLong("currentId") ?: 10000L
            val newUserId = currentId + 1

            // 1) Increment the userCounter
            transaction.update(settingsRef, "currentId", newUserId)

            // 2) Build the user doc with numeric ID
            val userRef = db.collection("users").document(newUserId.toString())
            val childProfile = mapOf(
                "childName" to childName,
                "age" to selectedAge,
                "avatar" to selectedAvatarName
            )

            val userData = mapOf(
                "parentName" to parentName,
                "email" to email,
                "userId" to newUserId.toString(),
                "xp" to 0,
                "chapters_completed" to emptyList<String>(),
                "stories_completed" to emptyList<String>(), // New field
                "levels_completed" to emptyList<String>(),  // New field
                "earned_badges" to emptyList<String>(),
                "quizzes_attended" to 0,
                "streak" to 0,
                "childProfiles" to listOf(childProfile)
            )

            transaction.set(userRef, userData)

            // Return the new userId from the transaction so addOnSuccessListener can use it
            newUserId
        }.addOnSuccessListener { createdUserId ->
            viewModelScope.launch {
                dataStoreManager.saveUserDetails(
                    email = email,
                    password = "", // or keep original password if you want
                    userId = createdUserId.toString()
                )
            }
            fetchAndSaveFcmToken(createdUserId.toString())
            // Notify success
            _profileCreationState.value = ProfileCreationState.Success
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Transaction failed.", e)
            _profileCreationState.value = ProfileCreationState.Error(e.localizedMessage ?: "Unknown error")
        }
    }
    suspend fun updateTimeZoneIfNeeded(context: Context, userId: String) {
        val currentTimeZone = TimeZone.getDefault().id
        // Retrieve the locally stored time zone if needed (or use null if not set)
        val storedTimeZone = dataStoreManager.getTimeZone(context)
        if (storedTimeZone == currentTimeZone) {
            Log.d("TimeZone", "Time zone unchanged: $currentTimeZone")
            return
        }
        dataStoreManager.saveTimeZone(context, currentTimeZone)
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update("timeZone", currentTimeZone)
            .addOnSuccessListener {
                Log.d("TimeZone", "User time zone updated in Firestore: $currentTimeZone")
            }
            .addOnFailureListener { e ->
                Log.e("TimeZone", "Error updating time zone: ${e.localizedMessage}")
            }
    }



    fun fetchUserProfile() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first() // Get userId from DataStore
            if (!userId.isNullOrEmpty()) {
                try {
                    val documentSnapshot = fireStore.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    if (documentSnapshot.exists()) {
                        val parentName = documentSnapshot.getString("parentName") ?: "N/A"
                        val email = documentSnapshot.getString("email") ?: "N/A"
                        val userId = documentSnapshot.getString("userId") ?: "N/A"

                        val childProfiles = documentSnapshot.get("childProfiles") as? List<Map<String, Any>> ?: emptyList()
                        val xpFromDoc = (documentSnapshot.getLong("xp") ?: 0).toInt()
                        val chaptersFromDoc = documentSnapshot.get("chapters_completed") as? List<String> ?: emptyList()
                        val storiesFromDoc = documentSnapshot.get("stories_completed") as? List<String> ?: emptyList()
                        val levelsFromDoc = documentSnapshot.get("levels_completed") as? List<String> ?: emptyList()

                        if (childProfiles.isNotEmpty()) {
                            val childProfile = childProfiles[0]
                            val childName = childProfile["childName"] as? String ?: "N/A"
                            val age = (childProfile["age"] as? Long)?.toInt() ?: 0
                            val avatar = childProfile["avatar"] as? String ?: "N/A"

                            _userProfileState.value = UserProfile(
                                parentName,
                                childName,
                                age,
                                avatar,
                                email,
                                userId,
                                xp = xpFromDoc,
                                chaptersCompleted = chaptersFromDoc,
                                storiesCompleted = storiesFromDoc,
                                levelsCompleted = levelsFromDoc
                            )
                        } else {
                            _userProfileState.value = UserProfile(
                                parentName,
                                "",
                                0,
                                "",
                                email,
                                userId,
                                xp = xpFromDoc,
                                chaptersCompleted = chaptersFromDoc,
                                storiesCompleted = storiesFromDoc,
                                levelsCompleted = levelsFromDoc
                            )
                        }
                    } else {
                        Log.d("AuthViewModel", "User document does not exist")
                    }
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error fetching user profile: ${e.localizedMessage}")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()

            // Clear user details from DataStore
            dataStoreManager.clearUserDetails()

            // Optionally clear any in-memory state (e.g., userProfileState)
            _userProfileState.value = null
        }
    }



}


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
)


private val _profileCreationState = MutableStateFlow<ProfileCreationState>(ProfileCreationState.Idle)
    val profileCreationState: StateFlow<ProfileCreationState> get() = _profileCreationState

sealed class ProfileCreationState {
    object Idle : ProfileCreationState()
    object Success : ProfileCreationState()
    data class Error(val message: String) : ProfileCreationState()
}


fun fetchAndSaveFcmToken(userId: String) {
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Fetched Token: $token")
            // Save token to Firestore
            saveFcmToken(userId, token)
        }
}

private fun saveFcmToken(userId: String, token: String) {
    FirebaseFirestore.getInstance().collection("users")
        .document(userId)
        .update("fcmToken", token)
        .addOnSuccessListener { Log.d("FCM", "FCM token updated in Firestore") }
        .addOnFailureListener { e -> Log.e("FCM", "Error: ${e.localizedMessage}") }
}









sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
    object NeedsProfile : AuthState()
}
