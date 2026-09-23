package com.alifba.alifba.presenation.login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.features.authentication.DataStoreManager
import com.alifba.alifba.features.authentication.OnboardingDataStoreManager
import com.alifba.alifba.features.authentication.usecase.SignInUseCase
import com.alifba.alifba.features.authentication.usecase.SignUpUseCase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import com.posthog.PostHog
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.interfaces.LogInCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val onboardingDataStoreManager: OnboardingDataStoreManager,
    @ApplicationContext private val appContext: Context,
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    val dataStoreManager: DataStoreManager,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private var hasIdentifiedThisSession = false
    val email: StateFlow<String?> = dataStoreManager.email
    val userId: StateFlow<String?> = dataStoreManager.userId

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> get() = _authState
    private var pendingAuthAction: PendingAuthAction? = null
    private var pendingLinkCredential: AuthCredential? = null
    private var pendingLinkEmail: String? = null
    data class ProviderSuggestion(
        val providerId: String,
        val message: String
    )
    private val _providerSuggestion = MutableStateFlow<ProviderSuggestion?>(null)
    val providerSuggestion: StateFlow<ProviderSuggestion?> get() = _providerSuggestion
    private val _needsContactEmailPrompt = MutableStateFlow(false)
    val needsContactEmailPrompt: StateFlow<Boolean> get() = _needsContactEmailPrompt

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
            _providerSuggestion.value = null
            val result = signUpUseCase(email, password)
            if (result.isSuccess) {
                val createdUserId = auth.currentUser?.uid
                if (!createdUserId.isNullOrBlank()) {
                    dataStoreManager.saveUserDetails(email, createdUserId)
                    ensureUserDocument(createdUserId, email)
                    identifyUser(
                        userId = createdUserId,
                        email = email,
                        name = null,
                        authDateKey = "signup_date"
                    )
                    captureAuthEvent(
                        eventName = "user_signed_up",
                        userId = createdUserId,
                        email = email
                    )
                    flushPostHog()
                    updateSubscriptionStatus(createdUserId, email)
                }

                // Send verification email
                sendEmailVerification(
                    onSuccess = {
                        viewModelScope.launch {
                            pendingAuthAction = PendingAuthAction(
                                email = email,
                                isSignUp = true
                            )
                            _authState.value = AuthState.AwaitingEmailVerification(
                                email = email,
                                isSignUp = true
                            )
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
                val resolved = resolveProviderSpecificError(
                    email = email,
                    isSignIn = false,
                    fallbackMessage = msg
                )
                _authState.value = AuthState.Error(resolved)
                onError(resolved)
            }
        }
    }

    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _providerSuggestion.value = null
            val result = signInUseCase(email, password)
            if (result.isSuccess) {
                val currentUser = auth.currentUser
                if (currentUser?.isEmailVerified == true) {
                    // Email is verified, proceed with login
                    fetchUserData(email, onResult, onError)
                } else {
                    // Automatically resend verification and update state
                    pendingAuthAction = PendingAuthAction(
                        email = email,
                        isSignUp = false
                    )
                    currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("AuthViewModel", "Verification email resent automatically.")
                        } else {
                            Log.w("AuthViewModel", "Failed to resend verification email automatically.", task.exception)
                        }
                        // Set state regardless of whether the resend succeeded, so UI can navigate
                        _authState.value = AuthState.AwaitingEmailVerification(
                            email = email,
                            isSignUp = false
                        )
                    }
                }
            } else {
                val msg = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                val resolved = resolveProviderSpecificError(
                    email = email,
                    isSignIn = true,
                    fallbackMessage = msg
                )
                _authState.value = AuthState.Error(resolved)
                onError(resolved)
            }
        }
    }

    // onResult reports (hasProfiles, isNewUser) — isNewUser lets the caller distinguish a
    // brand-new OAuth signup from a returning sign-in, since Google/Apple share one entry
    // point for both (unlike email, which has separate signUp/signIn functions).
    fun signInWithGoogle(
        idToken: String,
        onResult: (Boolean, Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            signInWithCredential(credential, onResult, onError)
        }
    }

    fun signInWithApple(
        activity: Activity,
        onResult: (Boolean, Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val pendingResultTask = auth.pendingAuthResult
                val authResult = if (pendingResultTask != null) {
                    pendingResultTask.await()
                } else {
                    val provider = buildAppleProvider()
                    val wasAnonymous = auth.currentUser?.isAnonymous == true
                    val result = if (wasAnonymous) {
                        auth.currentUser?.startActivityForLinkWithProvider(activity, provider)?.await()
                    } else {
                        auth.startActivityForSignInWithProvider(activity, provider).await()
                    }
                    if (wasAnonymous) {
                        // See signInWithCredential's comment: linking doesn't itself refresh the
                        // ID token Firestore is using, so without this the Home screen's
                        // chapters listener keeps failing against the stale anonymous token for
                        // the rest of this app session.
                        auth.currentUser?.getIdToken(true)?.await()
                    }
                    result
                }

                val isNewUser = authResult?.additionalUserInfo?.isNewUser == true
                val user = authResult?.user ?: auth.currentUser
                fetchUserData(user?.email, onResult = { hasProfiles -> onResult(hasProfiles, isNewUser) }, onError)
            } catch (e: FirebaseAuthUserCollisionException) {
                handleAccountExistsWithDifferentCredential(e, e.updatedCredential, onError)
            } catch (e: FirebaseAuthException) {
                val msg = mapAppleSignInError(e)
                _authState.value = AuthState.Error(msg)
                onError(msg)
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Apple sign-in failed"
                _authState.value = AuthState.Error(msg)
                onError(msg)
            }
        }
    }

    private fun buildAppleProvider(): OAuthProvider {
        return OAuthProvider.newBuilder("apple.com")
            .setScopes(listOf("email", "name"))
            .addCustomParameter("locale", Locale.getDefault().toLanguageTag())
            .build()
    }

    private fun mapAppleSignInError(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_WEB_CONTEXT_CANCELED" -> "Apple sign-in was canceled."
            "ERROR_WEB_CONTEXT_ALREADY_PRESENTED" -> "Apple sign-in is already in progress."
            "ERROR_OPERATION_NOT_ALLOWED" -> "Apple sign-in is not enabled in Firebase yet."
            "ERROR_INVALID_CREDENTIAL" -> "Apple sign-in is misconfigured. Verify Apple provider credentials in Firebase."
            "ERROR_WEB_NETWORK_REQUEST_FAILED" -> "Network error while connecting to Apple. Please try again."
            else -> exception.localizedMessage ?: "Apple sign-in failed"
        }
    }

    private suspend fun signInWithCredential(
        credential: AuthCredential,
        onResult: (Boolean, Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val wasAnonymous = auth.currentUser?.isAnonymous == true
            val authResult = if (wasAnonymous) {
                auth.currentUser?.linkWithCredential(credential)?.await()
            } else {
                auth.signInWithCredential(credential).await()
            }
            if (wasAnonymous) {
                // Firestore keeps using the ID token that was valid when it first connected —
                // linking an anonymous session to a real credential doesn't itself push a fresh
                // token, so without this force-refresh, security-rule-gated reads (like the
                // Home screen's chapters listener) keep evaluating against the stale anonymous
                // token for the rest of this app session, only recovering on a full relaunch
                // (which re-initializes Auth from the now-linked persisted state).
                auth.currentUser?.getIdToken(true)?.await()
            }
            val isNewUser = authResult?.additionalUserInfo?.isNewUser == true
            fetchUserData(auth.currentUser?.email, onResult = { hasProfiles -> onResult(hasProfiles, isNewUser) }, onError)
        } catch (e: FirebaseAuthUserCollisionException) {
            handleAccountExistsWithDifferentCredential(e, credential, onError)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Sign-in failed"
            _authState.value = AuthState.Error(msg)
            onError(msg)
        }
    }

    private suspend fun handleAccountExistsWithDifferentCredential(
        exception: FirebaseAuthUserCollisionException,
        credential: AuthCredential?,
        onError: (String) -> Unit
    ) {
        pendingLinkCredential = credential
        pendingLinkEmail = exception.email
        val providerHint = try {
            val methods = exception.email?.let {
                auth.fetchSignInMethodsForEmail(it).await().signInMethods
            } ?: emptyList<String>()
            methods.joinToString()
        } catch (_: Exception) {
            ""
        }
        val message = if (providerHint.isNotBlank()) {
            "Account exists. Please sign in with: $providerHint, then we'll link this provider."
        } else {
            "Account exists with a different sign-in method. Please sign in with the existing provider to link."
        }
        _authState.value = AuthState.Error(message)
        onError(message)
    }

    private fun fetchUserData(
        email: String?,
        onResult: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                    ?: throw Exception("No authenticated user.")
                val uid = currentUser.uid
                val primaryEmail = email ?: currentUser.email

                val userDoc = ensureUserDocument(uid, primaryEmail)
                dataStoreManager.saveUserDetails(primaryEmail, uid)
                _providerSuggestion.value = null

                identifyUser(
                    userId = uid,
                    email = primaryEmail ?: "",
                    name = userDoc.getString("parentName"),
                    authDateKey = "signin_date"
                )
                captureAuthEvent(
                    eventName = "user_signed_in",
                    userId = uid,
                    email = primaryEmail ?: ""
                )
                flushPostHog()

                linkPendingCredentialIfAny()
                fetchAndSaveFcmToken(uid)

                // Sync RevenueCat app user with this Alifba user
                syncRevenueCatUser(uid)

                // Set onboarding as completed for existing users during signin
                onboardingDataStoreManager.setOnboardingCompleted(true)

                if (currentUser.email.isNullOrBlank()) {
                    _needsContactEmailPrompt.value = true
                }

                val hasProfiles = checkForChildProfiles()
                _authState.value = AuthState.Success
                onResult(hasProfiles)
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Unknown error"
                _authState.value = AuthState.Error(msg)
                onError(msg)
            }
        }
    }

    fun clearProviderSuggestion() {
        _providerSuggestion.value = null
    }

    private suspend fun resolveProviderSpecificError(
        email: String?,
        isSignIn: Boolean,
        fallbackMessage: String
    ): String {
        if (email.isNullOrBlank()) return fallbackMessage
        return try {
            val methods = auth.fetchSignInMethodsForEmail(email).await().signInMethods
                ?: emptyList<String>()
            if (methods.isEmpty()) return fallbackMessage

            val hasPassword = methods.contains("password")
            if (isSignIn && hasPassword) {
                // User should enter the correct password.
                return fallbackMessage
            }
            if (!isSignIn && hasPassword) {
                return "This email is already registered. Please log in."
            }

            return when {
                methods.contains("google.com") -> {
                    val msg = "This email is linked to Google. Please continue with Google."
                    _providerSuggestion.value = ProviderSuggestion("google.com", msg)
                    msg
                }
                methods.contains("apple.com") -> {
                    val msg = "This email is linked to Apple. Please continue with Apple."
                    _providerSuggestion.value = ProviderSuggestion("apple.com", msg)
                    msg
                }
                else -> "This email uses a different sign-in method. Please use that method."
            }
        } catch (_: Exception) {
            fallbackMessage
        }
    }

    suspend fun checkForChildProfiles(): Boolean {
        val currentUid = dataStoreManager.userId.first() ?: auth.currentUser?.uid
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
        val email = currentUser.email.orEmpty()

        // 2) Build new multi-profile structure
        val childProfile = ChildProfile(
            childName = childName,
            age = selectedAge ?: 0,
            avatar = selectedAvatarName
        )

        val userData = mutableMapOf<String, Any>(
            "parentName" to parentName,
            "userId" to uid,
            "profiles" to listOf(childProfile.toMap()),
            "createdAt" to System.currentTimeMillis(),
            "lastUpdated" to System.currentTimeMillis()
        )
        if (email.isNotBlank()) {
            userData["primaryEmail"] = email
        }

        // 3) Write data directly (merge to preserve existing fields)
        firestore.collection("users")
            .document(uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                viewModelScope.launch {
                    // Save details in DataStore
                    dataStoreManager.saveUserDetails(
                        email = email,
                        userId = uid
                    )
                    upsertDevice(uid, activeProfileId = childProfile.profileId)
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
            val currentUid = dataStoreManager.userId.first() ?: auth.currentUser?.uid
            if (currentUid.isNullOrEmpty()) {
                Log.d("AuthViewModel", "No userId found in DataStore.")
                return@launch
            }

            try {
                val docSnap = ensureUserDocument(
                    userId = currentUid,
                    primaryEmail = auth.currentUser?.email ?: dataStoreManager.email.first()
                )

                if (!docSnap.exists()) {
                    Log.d("AuthViewModel", "User document does not exist.")
                    return@launch
                }

                val deviceActiveProfileId = getDeviceActiveProfileId(currentUid)

                // Load multi-profile structure
                val resolvedActiveProfileId = loadMultiProfileData(docSnap, deviceActiveProfileId)
                if (!resolvedActiveProfileId.isNullOrBlank()) {
                    upsertDevice(currentUid, activeProfileId = resolvedActiveProfileId)
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user profile: ${e.localizedMessage}")
            }
        }
    }

    private fun loadMultiProfileData(
        docSnap: com.google.firebase.firestore.DocumentSnapshot,
        deviceActiveProfileId: String?
    ): String? {
        try {
            // Read premium status from DataStore (StateFlow)
            val premium = dataStoreManager.isPremium.value
            var needsProfileIdUpdate = false
            val parentAccount = ParentAccount(
                parentName = docSnap.getString("parentName") ?: "",
                primaryEmail = docSnap.getString("primaryEmail")
                    ?: docSnap.getString("email"),
                contactEmail = docSnap.getString("contactEmail"),
                userId = docSnap.getString("userId") ?: "",
                profiles = (docSnap.get("profiles") as? List<Map<String, Any>> ?: emptyList()).map { profileMap ->
                    val profileId = profileMap["profileId"] as? String
                        ?: run {
                            needsProfileIdUpdate = true
                            java.util.UUID.randomUUID().toString()
                        }
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
                // Active profile will be derived from device-specific state.
                activeProfileIndex = 0,
                createdAt = docSnap.getLong("createdAt") ?: System.currentTimeMillis(),
                lastUpdated = docSnap.getLong("lastUpdated") ?: System.currentTimeMillis()
            )

            _parentAccountState.value = parentAccount

            // Ensure every profile has a stable profileId
            if (needsProfileIdUpdate) {
                val userId = docSnap.getString("userId")
                if (!userId.isNullOrEmpty()) {
                    viewModelScope.launch {
                        try {
                            firestore.collection("users").document(userId)
                                .update(
                                    "profiles",
                                    parentAccount.profiles.map { it.toMap() },
                                    "lastUpdated",
                                    System.currentTimeMillis()
                                ).await()
                        } catch (e: Exception) {
                            Log.e("AuthViewModel", "Failed to persist profileIds: ${e.message}")
                        }
                    }
                }
            }

            // Set the current active child profile
            val resolvedActiveProfileId = resolveActiveProfileId(
                parentAccount = parentAccount,
                deviceActiveProfileId = deviceActiveProfileId,
                isPremium = premium,
                legacyIndex = (docSnap.getLong("activeProfileIndex") ?: 0).toInt()
            )
            if (!resolvedActiveProfileId.isNullOrBlank()) {
                val activeIndex = parentAccount.profiles.indexOfFirst { it.profileId == resolvedActiveProfileId }
                if (activeIndex >= 0) {
                    _currentChildProfile.value = parentAccount.profiles[activeIndex]
                    _parentAccountState.value = parentAccount.copy(activeProfileIndex = activeIndex)
                }
            }

            Log.d("AuthViewModel", "Loaded parent account with ${parentAccount.profiles.size} profiles")

        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error loading multi-profile data: ${e.message}")
        }
        return _currentChildProfile.value?.profileId
    }


    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            dataStoreManager.clearUserDetails()
            _parentAccountState.value = null
            _currentChildProfile.value = null
            pendingAuthAction = null
            pendingLinkCredential = null
            pendingLinkEmail = null
            _needsContactEmailPrompt.value = false
            _authState.value = AuthState.Idle
            hasIdentifiedThisSession = false
            try {
                PostHog.reset()
            } catch (e: Exception) {
                Log.e("PostHog", "Failed to reset PostHog: ${e.message}", e)
            }
            try {
                Purchases.sharedInstance.logOut()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error logging out from RevenueCat: ${e.message}")
            }
            try {
                OneSignal.logout()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error logging out from OneSignal: ${e.message}")
            }
        }
    }

    fun resendVerificationEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            sendEmailVerification(
                onSuccess = {
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

    fun handleEmailVerified(
        onSignInResult: (Boolean) -> Unit,
        onSignUpVerified: () -> Unit,
        onError: (String) -> Unit
    ) {
        val pending = pendingAuthAction
        if (pending == null) {
            onError("No pending verification found.")
            return
        }
        pendingAuthAction = null
        if (pending.isSignUp) {
            _authState.value = AuthState.Success
            onSignUpVerified()
        } else {
            _authState.value = AuthState.Loading
            fetchUserData(pending.email, onSignInResult, onError)
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

    /**
     * Ensures RevenueCat is logged in with the same app user ID
     * as the currently signed-in Alifba account, so entitlements
     * follow the parent account across devices.
     */
    fun syncRevenueCatForCurrentUser() {
        viewModelScope.launch {
            val uid = dataStoreManager.userId.first()
            if (!uid.isNullOrBlank()) {
                syncRevenueCatUser(uid)
            }
        }
    }

    /**
     * Logs the current Alifba account into OneSignal keyed by the same Firebase uid used for
     * RevenueCat/Firestore elsewhere (previously this was keyed by email, which doesn't match
     * the identity used anywhere else in the app and made cross-referencing a OneSignal
     * subscription back to an app user unreliable). Tags/email are set only after login()
     * so they land on the correct OneSignal user rather than racing a still-anonymous one.
     */
    fun syncOneSignalForCurrentUser(userType: String) {
        viewModelScope.launch {
            val uid = dataStoreManager.userId.first()
            if (uid.isNullOrBlank()) return@launch
            OneSignal.login(uid)
            OneSignal.User.addTag("user_type", userType)
            if (userType == "new_user") {
                OneSignal.User.addTag("send_welcome_email", "true")
                tagSignupCompleted()
            }
            auth.currentUser?.email?.let { OneSignal.User.addEmail(it) }
        }
    }

    // Tags the OneSignal profile at the moment an account is first created, so a OneSignal
    // Journey can use "tag signup_completed_at is set" as its entry trigger (the SDK version
    // pinned in this project predates the trackEvent/Custom Events API).
    private fun tagSignupCompleted() {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
        OneSignal.User.addTag("signup_completed_at", timestamp)
    }

    private fun syncRevenueCatUser(appUserId: String) {
        try {
            Purchases.sharedInstance.logIn(
                appUserId,
                object : LogInCallback {
                    override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                        val isPro = customerInfo.entitlements.active.containsKey("Alifba Pro")
                        viewModelScope.launch {
                            dataStoreManager.setPremium(isPro)
                        }
                        viewModelScope.launch {
                            val email = dataStoreManager.email.first() ?: auth.currentUser?.email
                            val distinctId = resolveDistinctId(email, appUserId)
                            if (distinctId.isNotBlank()) {
                                identifySubscriptionStatus(distinctId, appUserId, isPro)
                            }
                        }
                    }

                    override fun onError(error: PurchasesError) {
                        Log.e("AuthViewModel", "RevenueCat logIn error: ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("AuthViewModel", "RevenueCat logIn threw: ${e.message}")
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

                viewModelScope.launch {
                    try {
                        upsertDevice(userId, fcmToken = token)
                        Log.d("FCM", "FCM token updated in device doc")
                    } catch (e: Exception) {
                        Log.e("FCM", "Error updating device doc: ${e.localizedMessage}")
                    }
                }
            }
    }

    fun updateContactEmail(contactEmail: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = dataStoreManager.userId.first() ?: auth.currentUser?.uid
                if (uid.isNullOrBlank()) {
                    onComplete(false)
                    return@launch
                }
                firestore.collection("users").document(uid)
                    .update(
                        "contactEmail",
                        contactEmail.trim(),
                        "lastUpdated",
                        System.currentTimeMillis()
                    ).await()
                if (dataStoreManager.email.first().isNullOrBlank()) {
                    dataStoreManager.saveUserDetails(contactEmail.trim(), uid)
                }
                _needsContactEmailPrompt.value = false
                onComplete(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update contact email: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun dismissContactEmailPrompt() {
        _needsContactEmailPrompt.value = false
    }

    private suspend fun ensureUserDocument(
        userId: String,
        primaryEmail: String?
    ): com.google.firebase.firestore.DocumentSnapshot {
        val userRef = firestore.collection("users").document(userId)
        val existing = userRef.get().await()
        if (existing.exists()) {
            if (!primaryEmail.isNullOrBlank() && existing.getString("primaryEmail").isNullOrBlank()) {
                userRef.update(
                    "primaryEmail",
                    primaryEmail,
                    "lastUpdated",
                    System.currentTimeMillis()
                ).await()
                return userRef.get().await()
            }
            return existing
        }

        val userData = mutableMapOf<String, Any>(
            "userId" to userId,
            "parentName" to "",
            "profiles" to emptyList<Map<String, Any>>(),
            "createdAt" to System.currentTimeMillis(),
            "lastUpdated" to System.currentTimeMillis()
        )
        if (!primaryEmail.isNullOrBlank()) {
            userData["primaryEmail"] = primaryEmail
        }
        userRef.set(userData, SetOptions.merge()).await()
        return userRef.get().await()
    }

    private suspend fun getDeviceActiveProfileId(userId: String): String? {
        val deviceId = dataStoreManager.getOrCreateDeviceId()
        val deviceDoc = firestore.collection("users")
            .document(userId)
            .collection("devices")
            .document(deviceId)
            .get()
            .await()
        return deviceDoc.getString("activeProfileId")
    }

    private suspend fun upsertDevice(
        userId: String,
        activeProfileId: String? = null,
        fcmToken: String? = null
    ) {
        val deviceId = dataStoreManager.getOrCreateDeviceId()
        val payload = mutableMapOf<String, Any>(
            "deviceId" to deviceId,
            "platform" to "Android",
            "lastSeen" to System.currentTimeMillis()
        )
        if (!activeProfileId.isNullOrBlank()) {
            payload["activeProfileId"] = activeProfileId
        }
        if (!fcmToken.isNullOrBlank()) {
            payload["fcmToken"] = fcmToken
        }
        firestore.collection("users")
            .document(userId)
            .collection("devices")
            .document(deviceId)
            .set(payload, SetOptions.merge())
            .await()
    }

    private suspend fun clearDeviceActiveProfile(userId: String) {
        val deviceId = dataStoreManager.getOrCreateDeviceId()
        firestore.collection("users")
            .document(userId)
            .collection("devices")
            .document(deviceId)
            .update(
                "activeProfileId",
                FieldValue.delete(),
                "lastSeen",
                System.currentTimeMillis()
            )
            .await()
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

    private suspend fun linkPendingCredentialIfAny() {
        val pending = pendingLinkCredential ?: return
        try {
            auth.currentUser?.linkWithCredential(pending)?.await()
            // See signInWithCredential's comment on the same pattern — force a fresh ID token
            // after any credential change so Firestore doesn't keep evaluating security rules
            // against stale token claims for the rest of this app session.
            auth.currentUser?.getIdToken(true)?.await()
            pendingLinkCredential = null
            pendingLinkEmail = null
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to link pending credential: ${e.message}")
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

                val updatedProfiles = currentParent.profiles + newChildProfile
                val updatedAt = System.currentTimeMillis()

                // Update Firestore (profiles only)
                firestore.collection("users").document(currentParent.userId)
                    .update(
                        "profiles",
                        updatedProfiles.map { it.toMap() },
                        "lastUpdated",
                        updatedAt
                    ).await()

                // Update local state
                val updatedParent = currentParent.copy(
                    profiles = updatedProfiles,
                    lastUpdated = updatedAt
                )
                _parentAccountState.value = updatedParent

                if (_currentChildProfile.value == null) {
                    _currentChildProfile.value = newChildProfile
                    upsertDevice(currentParent.userId, activeProfileId = newChildProfile.profileId)
                }

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

            // Block switching to extra profiles for non-premium users
            val premium = dataStoreManager.isPremium.value
            if (!premium && profileIndex > 0) {
                Log.d("Profile", "Blocked switching to profile index $profileIndex for non-premium user")
                return@launch
            }

            val selectedProfile = currentParent.profiles[profileIndex]
            val updatedParent = currentParent.copy(
                activeProfileIndex = profileIndex,
                lastUpdated = System.currentTimeMillis()
            )

            try {
                upsertDevice(currentParent.userId, activeProfileId = selectedProfile.profileId)
                firestore.collection("users").document(currentParent.userId)
                    .update("lastUpdated", System.currentTimeMillis()).await()

                // Update local state
                _parentAccountState.value = updatedParent
                _currentChildProfile.value = selectedProfile
                Log.d("Profile", "Switched to profile: ${selectedProfile.childName}")
                
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

                // Update Firestore (profiles only)
                firestore.collection("users").document(currentParent.userId)
                    .update(
                        "profiles",
                        updatedProfiles.map { it.toMap() },
                        "lastUpdated",
                        System.currentTimeMillis()
                    ).await()

                val newActiveProfile = if (updatedProfiles.isNotEmpty()) {
                    updatedProfiles[newActiveIndex]
                } else {
                    null
                }

                if (newActiveProfile != null) {
                    upsertDevice(currentParent.userId, activeProfileId = newActiveProfile.profileId)
                } else {
                    clearDeviceActiveProfile(currentParent.userId)
                }

                // Update local state
                _parentAccountState.value = updatedParent
                _currentChildProfile.value = newActiveProfile
                Log.d("Profile", "Removed profile: ${removedProfile.childName}")
                
            } catch (e: Exception) {
                Log.e("Profile", "Failed to remove profile: ${e.message}")
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.localizedMessage ?: "An unknown error occurred.")
                }
            }
    }

    fun updateSubscriptionStatusForCurrentUser() {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first()
            val email = dataStoreManager.email.first() ?: auth.currentUser?.email
            if (!userId.isNullOrBlank() || !email.isNullOrBlank()) {
                updateSubscriptionStatus(userId, email)
            }
        }
    }

    fun updateLastActivityForCurrentUser() {
        viewModelScope.launch {
            val email = dataStoreManager.email.first() ?: auth.currentUser?.email
            val userId = dataStoreManager.userId.first() ?: auth.currentUser?.uid
            val distinctId = resolveDistinctId(email, userId)
            if (distinctId.isNotBlank()) {
                identifyLastActivity(distinctId)
            }
        }
    }

    suspend fun identifyCurrentUserForAppOpen() {
        if (hasIdentifiedThisSession) {
            return
        }
        val email = dataStoreManager.email.first() ?: auth.currentUser?.email
        val userId = dataStoreManager.userId.first() ?: auth.currentUser?.uid
        if (email.isNullOrBlank() && userId.isNullOrBlank()) {
            return
        }
        identifyUser(
            userId = userId ?: "",
            email = email ?: "",
            name = null,
            authDateKey = "signin_date"
        )
        flushPostHog()
        if (!userId.isNullOrBlank()) {
            upsertDevice(userId)
        }
        hasIdentifiedThisSession = true
    }

    private fun identifyUser(userId: String, email: String, name: String?, authDateKey: String) {
        try {
            val normalizedEmail = email.trim()
            val distinctId = resolveDistinctId(normalizedEmail, userId)
            val properties = mutableMapOf<String, Any>(
                "platform" to "Android",
                "install_date" to getOrCreateInstallDate(),
                authDateKey to nowIsoUtc(),
                "last_activity" to nowIsoUtc()
            )
            if (normalizedEmail.isNotBlank()) {
                properties["email"] = normalizedEmail
            }
            if (userId.isNotBlank()) {
                properties["user_id"] = userId
            }
            if (!name.isNullOrBlank()) {
                properties["name"] = name
            }
            PostHog.identify(
                distinctId = distinctId,
                userProperties = properties
            )
        } catch (e: Exception) {
            Log.e("PostHog", "Failed to identify user: ${e.message}", e)
        }
    }

    private fun updateSubscriptionStatus(userId: String?, email: String?) {
        try {
            val distinctId = resolveDistinctId(email, userId)
            if (distinctId.isBlank()) {
                return
            }
            Purchases.sharedInstance.getCustomerInfoWith(
                onError = { error ->
                    Log.e("PostHog", "Failed to fetch subscription status: ${error.message}")
                },
                onSuccess = { customerInfo ->
                    val isPro = customerInfo.entitlements.active.containsKey("Alifba Pro")
                    viewModelScope.launch {
                        dataStoreManager.setPremium(isPro)
                    }
                    identifySubscriptionStatus(distinctId, userId, isPro)
                }
            )
        } catch (e: Exception) {
            Log.e("PostHog", "Failed to update subscription status: ${e.message}", e)
        }
    }

    private fun identifySubscriptionStatus(distinctId: String, userId: String?, isPremium: Boolean) {
        try {
            val properties = mutableMapOf<String, Any>(
                "subscription_status" to if (isPremium) "premium" else "free"
            )
            if (!userId.isNullOrBlank()) {
                properties["user_id"] = userId
            }
            PostHog.identify(
                distinctId = distinctId,
                userProperties = properties
            )
        } catch (e: Exception) {
            Log.e("PostHog", "Failed to identify subscription status: ${e.message}", e)
        }
    }

    private fun identifyLastActivity(distinctId: String) {
        try {
            PostHog.identify(
                distinctId = distinctId,
                userProperties = mapOf(
                    "last_activity" to nowIsoUtc()
                )
            )
        } catch (e: Exception) {
            Log.e("PostHog", "Failed to identify last activity: ${e.message}", e)
        }
    }

    private fun captureAuthEvent(eventName: String, userId: String, email: String) {
        try {
            val properties = mapOf(
                "user_id" to userId,
                "email" to email,
                "platform" to "Android",
                "auth_method" to "email_password"
            )
            PostHog.capture(event = eventName, properties = properties)
        } catch (e: Exception) {
            Log.e("PostHog", "Failed to capture $eventName: ${e.message}", e)
        }
    }

    private fun flushPostHog() {
        try {
            PostHog.flush()
        } catch (e: Exception) {
            Log.e("PostHog", "Failed to flush PostHog: ${e.message}", e)
        }
    }

    private fun resolveDistinctId(email: String?, userId: String? = null): String {
        val normalizedEmail = email?.trim()
        return if (!normalizedEmail.isNullOrBlank()) {
            normalizedEmail
        } else {
            userId?.trim().orEmpty()
        }
    }

    private fun getOrCreateInstallDate(): String {
        val prefs = appContext.getSharedPreferences("analytics_prefs", Context.MODE_PRIVATE)
        val existing = prefs.getString("install_date", null)
        if (!existing.isNullOrBlank()) {
            return existing
        }
        val now = nowIsoUtc()
        prefs.edit().putString("install_date", now).apply()
        return now
    }

    private fun nowIsoUtc(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
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


// Multi-profile data structure
data class ParentAccount(
    val parentName: String,
    val primaryEmail: String? = null,
    val contactEmail: String? = null,
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
    data class AwaitingEmailVerification(val email: String, val isSignUp: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
    object NeedsProfile : AuthState()
}

private data class PendingAuthAction(
    val email: String,
    val isSignUp: Boolean
)
