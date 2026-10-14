package com.alifba.alifba.presenation.login

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.features.authentication.dataStore
import com.alifba.alifba.presenation.profile.ProfileAgeGrid
import com.alifba.alifba.presenation.profile.ProfileAgeLabel
import com.alifba.alifba.presenation.profile.ProfileSetupLayout
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.widgets.AuthTextField
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileRegistration(
    navController: NavController
) {
    var isLoading by remember { mutableStateOf(false) } // Loading state
    val authViewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var childName by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var selectedAge by remember { mutableStateOf<Int?>(null) }
    val selectedAvatarName = remember { mutableStateOf("") }
    var navigateTo by remember { mutableStateOf<String?>(null) }
    val parentNameRequester = remember { BringIntoViewRequester() }
    val childNameRequester = remember { BringIntoViewRequester() }
    // Only offered when there is a screen to go back to (sign-up usually clears it)
    val canGoBack = remember { navController.previousBackStackEntry != null }

    val profileCreationState by authViewModel.profileCreationState.collectAsState()

    // Pre-fill from the name captured on the first onboarding screen, if this user came
    // through onboarding. Read directly off the same DataStore OnboardingDataStoreManager
    // wraps (see onboarding_child_name there) rather than injecting the manager itself here,
    // since this composable already has the Context it needs via LocalContext.
    LaunchedEffect(Unit) {
        val savedName = context.dataStore.data.firstOrNull()
            ?.get(stringPreferencesKey("onboarding_child_name"))
        if (!savedName.isNullOrBlank() && childName.isBlank()) {
            childName = savedName
        }
    }

    LaunchedEffect(profileCreationState) {
        when (profileCreationState) {
            is ProfileCreationState.Success -> {
                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()

                // Navigate to profile selection after registration success
                navController.navigate("profileSelection") {
                    popUpTo("createProfile") { inclusive = true }
                }
            }
            is ProfileCreationState.Error -> {
                val errorMessage = (profileCreationState as ProfileCreationState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    // Scrolls a field into view once the keyboard has opened under it
    fun Modifier.keepVisible(requester: BringIntoViewRequester) = this
        .bringIntoViewRequester(requester)
        .onFocusEvent { focusState ->
            if (focusState.hasFocus) {
                coroutineScope.launch {
                    delay(300)
                    requester.bringIntoView()
                }
            }
        }

    ProfileSetupLayout(
        title = "Let’s set up your family",
        selectedAvatarName = selectedAvatarName,
        onBack = if (canGoBack) ({ navController.popBackStack() }) else null
    ) {
        AuthTextField(
            value = parentName,
            onValueChange = { parentName = it },
            placeholder = "Your name",
            icon = Icons.Outlined.People,
            label = "Parent name",
            capitalization = KeyboardCapitalization.Words,
            focusHalo = true,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .keepVisible(parentNameRequester)
        )
        AuthTextField(
            value = childName,
            onValueChange = { childName = it },
            placeholder = "Child’s name",
            icon = Icons.Outlined.Person,
            label = "Your child’s name",
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
            focusHalo = true,
            modifier = Modifier.keepVisible(childNameRequester)
        )

        ProfileAgeLabel(childName = childName)
        ProfileAgeGrid(selectedAge = selectedAge) { age ->
            focusManager.clearFocus()
            selectedAge = age
        }

        LessonPrimaryButton(
            title = if (isLoading) "Registering..." else "Bismillah, let’s go",
            modifier = Modifier.padding(top = 16.dp),
            onClick = {
                if (!isLoading) {
                    isLoading = true
                    authViewModel.sendProfileDataToFireStore(parentName, childName, selectedAge, selectedAvatarName.value)
                }
            }
        )
    }

    if (isLoading) {
        LottieAnimationLoading(
            showDialog = remember { mutableStateOf(true) },
            isTransparentBackground = true,
            onAnimationEnd = {
                navigateTo?.let { route ->
                    navController.navigate(route) {
                        popUpTo("createProfile") { inclusive = true }
                    }
                    navigateTo = null
                }
            }
        )
    }
}

data class Avatar(val id: Int, val name: String)

/** Every buddy, in carousel order; `name` is what gets saved */
val AllAvatars = listOf(
    Avatar(R.drawable.deenasaur, "Deenasaur"),
    Avatar(R.drawable.duallama, "Duallama"),
    Avatar(R.drawable.firdawsaur, "Firdawsaur"),
    Avatar(R.drawable.ihsaninguin, "Ihsaninguin"),
    Avatar(R.drawable.imamoth, "Imamoth"),
    Avatar(R.drawable.khilafox, "Khilafox"),
    Avatar(R.drawable.shukraf, "Shukraf"),
    Avatar(R.drawable.jannahbee, "Jannah Bee"),
    Avatar(R.drawable.qadragon, "Qadragon"),
    Avatar(R.drawable.sabracorn, "Sabracorn"),
    Avatar(R.drawable.sadiqling, "Sadiqling"),
    Avatar(R.drawable.sidqhog, "Sidqhog")
)
