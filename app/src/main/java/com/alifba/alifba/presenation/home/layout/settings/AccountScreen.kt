package com.alifba.alifba.presenation.home.layout.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.ui_components.widgets.AnswerCard
import com.alifba.alifba.ui_components.widgets.AnswerState
import com.alifba.alifba.ui_components.widgets.AuthTextField
import com.alifba.alifba.ui_components.widgets.AuthTheme
import com.alifba.alifba.ui_components.widgets.ConfirmSheet
import com.alifba.alifba.ui_components.widgets.ConfirmSheetContent
import com.alifba.alifba.ui_components.widgets.ConfirmSheetIcon
import com.alifba.alifba.ui_components.widgets.DotLottieView
import com.alifba.alifba.ui_components.widgets.LessonButtonKind
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonPrimaryButton
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.SheetScaffold
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun AccountScreen(authViewModel: AuthViewModel, navController: NavController) {
    val parentAccount = authViewModel.parentAccountState.collectAsState().value
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val k = LessonTheme.scale()

    var showLogoutSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
    }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }

    val parentName = parentAccount?.parentName?.trim().orEmpty()
    val email = parentAccount?.primaryEmail ?: parentAccount?.contactEmail ?: ""
    val userId = parentAccount?.userId.orEmpty()
    // First letter of the parent's name (or email) for the avatar badge
    val initial = (if (parentName.isNotEmpty()) parentName else email).firstOrNull()?.uppercase() ?: "A"
    // "CJ3c...Q093": the full ID is long, so show the first and last four characters
    val shortUserId = when {
        userId.isEmpty() -> "Loading..."
        userId.length > 10 -> "${userId.take(4)}...${userId.takeLast(4)}"
        else -> userId
    }

    Box(
        Modifier
            .background(Color(0xFFF6F8FD))
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Round back button on the left, centred title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "My account",
                    style = LessonTheme.question().copy(fontSize = (32 * k).sp, textAlign = TextAlign.Center)
                )
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    LessonCircleButton(
                        iconRes = R.drawable.backarrow,
                        contentDescription = "Back",
                        onClick = { navController.popBackStack() }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = LessonTheme.margin),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar badge with the parent's initial
                Box(modifier = Modifier.padding(bottom = 4.dp)) {
                    val diameter = (92 * k).dp
                    Box(
                        modifier = Modifier
                            .size(diameter)
                            .offset(y = 4.dp)
                            .background(LessonTheme.primaryEdge, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(diameter)
                            .background(LessonTheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            style = LessonTheme.question().copy(fontSize = (48 * k).sp, color = Color.White)
                        )
                    }
                }

                Text(
                    text = if (parentName.isEmpty()) "Parent" else parentName,
                    style = LessonTheme.question().copy(fontSize = (30 * k).sp, textAlign = TextAlign.Center),
                    modifier = Modifier.padding(top = 18.dp)
                )
                Text(
                    text = "Parent account",
                    style = LessonTheme.story().copy(
                        fontSize = (19 * k).sp,
                        color = AuthTheme.secondaryText,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsSectionLabel("Account details")

                    SettingsGroup {
                        AccountDetailRow(
                            label = "Parent name",
                            value = if (parentName.isEmpty()) "Loading..." else parentName,
                            icon = Icons.Outlined.Person
                        )
                        SettingsDivider()
                        AccountDetailRow(
                            label = "Email",
                            value = email.ifEmpty { "Not set" },
                            icon = Icons.Outlined.Email
                        )
                        SettingsDivider()
                        AccountDetailRow(
                            label = "Account ID",
                            value = shortUserId,
                            icon = Icons.Outlined.Tag
                        ) {
                            CopyChip(copied = copied, enabled = userId.isNotEmpty()) {
                                clipboard.setText(AnnotatedString(userId))
                                copied = true
                            }
                        }
                    }

                    // Log out: white raised button
                    Box(modifier = Modifier.padding(top = 28.dp)) {
                        AnswerCard(
                            state = AnswerState.Normal,
                            modifier = Modifier.fillMaxWidth().height((60 * k).dp),
                            onClick = { showLogoutSheet = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = null,
                                    tint = LessonTheme.primary,
                                    modifier = Modifier.size((24 * k).dp)
                                )
                                Text(
                                    text = "Log out",
                                    style = LessonTheme.answer().copy(fontSize = (22 * k).sp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 24.dp)
                            .heightIn(min = 48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                SoundEffectManager.playClickSound()
                                showDeleteSheet = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Delete account",
                            style = LessonTheme.answer().copy(fontSize = (20 * k).sp, color = LessonTheme.wrongEdge)
                        )
                    }
                }
            }
        }
    }

    if (showLogoutSheet) {
        ConfirmSheet(
            title = "Log out?",
            message = "You\u2019ll need to log back in to see your children\u2019s progress on this device.",
            primaryTitle = "Stay logged in",
            destructiveTitle = "Yes, log out",
            onPrimary = { showLogoutSheet = false },
            onDestructive = {
                showLogoutSheet = false
                authViewModel.logout() // Clears user data and signs out
                navController.navigate("login") {
                    popUpTo("homeScreen") { inclusive = true } // Clear back stack
                }
            }
        ) {
            ConfirmSheetIcon(Icons.AutoMirrored.Outlined.Logout, LessonTheme.primary, LessonTheme.sky)
        }
    }

    if (showDeleteSheet) {
        // Email/password accounts have to confirm with their password to re-authenticate
        val needsPassword = remember {
            FirebaseAuth.getInstance().currentUser?.providerData?.any { it.providerId == "password" } == true
        }
        DeleteAccountSheet(
            needsPassword = needsPassword,
            onKeep = { showDeleteSheet = false },
            onDelete = { password ->
                showDeleteSheet = false
                isDeleting = true
                deleteUserAccount(authViewModel, navController, password, context) {
                    isDeleting = false
                }
            }
        )
    }

    if (isDeleting) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            DotLottieView(name = "moon_waiting", modifier = Modifier.size(200.dp))
        }
    }
}

private val DeleteIconFace = Color(0xFFFCEAEA)

/** "Delete your account?" sheet; email/password accounts get one extra step to confirm the password */
@Composable
private fun DeleteAccountSheet(
    needsPassword: Boolean,
    onKeep: () -> Unit,
    onDelete: (String) -> Unit
) {
    val k = LessonTheme.scale()
    var askingPassword by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    SheetScaffold(onDismiss = onKeep) {
        if (!askingPassword) {
            ConfirmSheetContent(
                title = "Delete your account?",
                message = "This removes your account, every child\u2019s profile and all their progress. It can\u2019t be undone.",
                primaryTitle = "Keep my account",
                destructiveTitle = "Yes, delete everything",
                onPrimary = onKeep,
                onDestructive = { if (needsPassword) askingPassword = true else onDelete("") }
            ) {
                ConfirmSheetIcon(Icons.Outlined.Delete, LessonTheme.wrongEdge, DeleteIconFace)
            }
        } else {
            Box(modifier = Modifier.padding(top = 8.dp)) {
                ConfirmSheetIcon(Icons.Outlined.Delete, LessonTheme.wrongEdge, DeleteIconFace)
            }
            Text(
                text = "Confirm your password",
                style = LessonTheme.question().copy(fontSize = (28 * k).sp, textAlign = TextAlign.Center)
            )
            Text(
                text = "Enter your password to finish deleting your account.",
                style = LessonTheme.story().copy(
                    fontSize = (19 * k).sp,
                    color = Color(0xFF5A5F7A),
                    textAlign = TextAlign.Center,
                    lineHeight = 1.35.em
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Your password",
                icon = Icons.Outlined.Lock,
                isPassword = true,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done
            )
            LessonPrimaryButton(
                title = "Delete my account",
                kind = if (password.isEmpty()) LessonButtonKind.Disabled else LessonButtonKind.Wrong,
                onClick = { onDelete(password) }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        SoundEffectManager.playClickSound()
                        onKeep()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    style = LessonTheme.answer().copy(fontSize = (20 * k).sp, color = AuthTheme.secondaryText)
                )
            }
        }
    }
}

@Composable
private fun AccountDetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    trailing: @Composable (() -> Unit)? = null
) {
    val k = LessonTheme.scale()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size((40 * k).dp)
                .background(LessonTheme.sky, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LessonTheme.primary,
                modifier = Modifier.size((22 * k).dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = LessonTheme.story().copy(fontSize = (16 * k).sp, color = AuthTheme.secondaryText)
            )
            Text(
                text = value,
                style = LessonTheme.answer().copy(fontSize = (21 * k).sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        trailing?.invoke()
    }
}

/** Copies the full account ID; the label flips to "Copied" for a moment */
@Composable
private fun CopyChip(copied: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val k = LessonTheme.scale()
    val shape = RoundedCornerShape(14.dp)
    Box(modifier = Modifier.padding(bottom = 3.dp)) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled
                ) {
                    SoundEffectManager.playClickSound()
                    onClick()
                },
            propagateMinConstraints = true
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 3.dp)
                    .background(LessonTheme.lineEdge, shape)
            )
            Row(
                modifier = Modifier
                    .height((38 * k).dp)
                    .background(Color.White, shape)
                    .border(2.dp, LessonTheme.line, shape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (copied) Icons.Filled.Check else Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = LessonTheme.primaryEdge,
                    modifier = Modifier.size((18 * k).dp)
                )
                Text(
                    text = if (copied) "Copied" else "Copy",
                    style = LessonTheme.answer().copy(fontSize = (18 * k).sp, color = LessonTheme.primaryEdge)
                )
            }
        }
    }
}

fun deleteUserAccount(
    authViewModel: AuthViewModel,
    navController: NavController,
    userPassword: String,
    context: Context,
    onComplete: () -> Unit
) {
    authViewModel.deleteUserAccount(
        onSuccess = {
            navController.navigate("login") {
                popUpTo("homeScreen") { inclusive = true }
            }
            onComplete()
        },
        onError = { errorMessage ->
            // Show a toast for the error (e.g., wrong password)
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            Log.e("AccountScreen", "Error deleting account: $errorMessage")
            onComplete()
        },
        userPassword = userPassword
    )
}



