package com.alifba.alifba.presenation.Login

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val onboardingDataStoreManager: OnboardingDataStoreManager,
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    private val dataStoreManager: DataStoreManager,
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
                dataStoreManager.saveUserDetails(email, password, "")
                onboardingDataStoreManager.setOnboardingCompleted(false) // Ensure onboarding starts
                onSuccess() // Notify success
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
                        val userDocument = querySnapshot.documents[0]
                        val userId = userDocument.getString("userId")
                        if (userId != null) {
                            dataStoreManager.saveUserDetails(email, password, userId)
                            val hasProfiles = checkForChildProfiles()
                            onResult(hasProfiles) // Notify with the profile status
                        } else {
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

        val settingsRef = db.collection("settings").document("userCounter")

        settingsRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                // Create the "userCounter" document with initial "currentId"
                settingsRef.set(mapOf("currentId" to 10000L)).addOnSuccessListener {
                    // Proceed with the transaction
                    proceedWithTransaction(
                        db,
                        settingsRef,
                        email,
                        parentName,
                        childName,
                        selectedAge,
                        selectedAvatarName
                    )
                }.addOnFailureListener { e ->
                    Log.w("Firestore", "Error creating settings/userCounter document.", e)
                    _profileCreationState.value =
                        ProfileCreationState.Error(e.localizedMessage ?: "Unknown error")
                }
            } else {
                // Proceed with the transaction
                proceedWithTransaction(
                    db,
                    settingsRef,
                    email,
                    parentName,
                    childName,
                    selectedAge,
                    selectedAvatarName
                )
            }
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Error checking settings/userCounter document.", e)
            _profileCreationState.value =
                ProfileCreationState.Error(e.localizedMessage ?: "Unknown error")
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
            val snapshot = transaction.get(settingsRef)
            val currentId = snapshot.getLong("currentId") ?: 10000L
            val newUserId = currentId + 1

            // Update the counter
            transaction.update(settingsRef, "currentId", newUserId)

            // Create the child profile
            val childProfile = hashMapOf(
                "childName" to childName,
                "age" to selectedAge,
                "avatar" to selectedAvatarName
            )

            // Create user data
            val userData = hashMapOf(
                "email" to email,
                "userId" to newUserId.toString(),
                "parentName" to parentName,
                "childProfiles" to listOf(childProfile)
            )

            // Set the user document with newUserId as the document ID
            val userRef = db.collection("users").document(newUserId.toString())
            transaction.set(userRef, userData)

            // Return the new user ID
            newUserId
        }.addOnSuccessListener { newUserId ->
            Log.d(
                "Firestore",
                "User and child profile successfully written with userId: $newUserId"
            )
            viewModelScope.launch {
                dataStoreManager.saveUserProfileExists(true)
                dataStoreManager.saveUserId(newUserId.toString())
            }
            _profileCreationState.value = ProfileCreationState.Success
        }.addOnFailureListener { e ->
            Log.w("Firestore", "Transaction failure.", e)
            _profileCreationState.value =
                ProfileCreationState.Error(e.localizedMessage ?: "Unknown error")
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

                        if (childProfiles.isNotEmpty()) {
                            val childProfile = childProfiles[0]
                            val childName = childProfile["childName"] as? String ?: "N/A"
                            val age = (childProfile["age"] as? Long)?.toInt() ?: 0
                            val avatar = childProfile["avatar"] as? String ?: "N/A"



                            _userProfileState.value = UserProfile(parentName, childName, age, avatar,email,userId)
                        } else {
                            _userProfileState.value = UserProfile(parentName, "", 0, "",email,userId)
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
    val userId: String
)

private val _profileCreationState = MutableStateFlow<ProfileCreationState>(ProfileCreationState.Idle)
    val profileCreationState: StateFlow<ProfileCreationState> get() = _profileCreationState

sealed class ProfileCreationState {
    object Idle : ProfileCreationState()
    object Success : ProfileCreationState()
    data class Error(val message: String) : ProfileCreationState()
}








sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
    object NeedsProfile : AuthState()
}
