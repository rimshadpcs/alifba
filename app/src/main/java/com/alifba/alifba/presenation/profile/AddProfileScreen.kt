package com.alifba.alifba.presenation.profile

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.ui_components.dialogs.LottieAnimationLoading
import com.alifba.alifba.ui_components.widgets.AuthTextField
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var childName by remember { mutableStateOf("") }
    var selectedAge by remember { mutableStateOf<Int?>(null) }
    val selectedAvatarName = remember { mutableStateOf("") }
    val childNameRequester = remember { BringIntoViewRequester() }

    // Handle navigation after profile is added
    LaunchedEffect(isLoading) {
        if (isLoading) {
            // Wait a moment for the profile to be added
            kotlinx.coroutines.delay(2000)
            Toast.makeText(context, "Profile Added Successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    val trimmedName = childName.trim()

    ProfileSetupLayout(
        title = "Add a child",
        selectedAvatarName = selectedAvatarName,
        onBack = { navController.popBackStack() }
    ) {
        // Only Child Name input - no Parent Name
        AuthTextField(
            value = childName,
            onValueChange = { childName = it },
            placeholder = "Child’s name",
            icon = Icons.Outlined.Person,
            label = "Child’s name",
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
            focusHalo = true,
            modifier = Modifier
                .bringIntoViewRequester(childNameRequester)
                .onFocusEvent { focusState ->
                    if (focusState.hasFocus) {
                        coroutineScope.launch {
                            delay(300)
                            childNameRequester.bringIntoView()
                        }
                    }
                }
        )

        ProfileAgeLabel(childName = childName)
        ProfileAgeGrid(selectedAge = selectedAge) { age ->
            focusManager.clearFocus()
            selectedAge = age
        }

        LessonPrimaryButton(
            title = when {
                isLoading -> "Adding..."
                trimmedName.isEmpty() -> "Add child"
                else -> "Add $trimmedName"
            },
            modifier = Modifier.padding(top = 16.dp),
            onClick = {
                if (!isLoading && childName.isNotBlank() && selectedAge != null && selectedAvatarName.value.isNotBlank()) {
                    isLoading = true
                    authViewModel.addNewChildProfile(
                        childName = childName,
                        age = selectedAge!!,
                        avatar = selectedAvatarName.value
                    )
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (isLoading) {
        LottieAnimationLoading(
            showDialog = remember { mutableStateOf(true) },
            isTransparentBackground = true,
            onAnimationEnd = {}
        )
    }
}
