package com.alifba.alifba.presenation.home.layout.settings

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.AlertDialog
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ButtonDefaults
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Card
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Divider
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alifba.alifba.R
import com.alifba.alifba.presenation.login.AuthViewModel
import com.alifba.alifba.ui_components.theme.darkRed
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightRed
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(authViewModel: AuthViewModel, navController: NavController) {
    val userProfile = authViewModel.userProfileState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )

    // Replace the background color with white and use black for borders
    val cardBackground = white
    val cardBorder = Color.Black
    val iconTint = navyBlue
    val textHighlightColor = navyBlue

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Load Lottie composition
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.account)
    )

    LaunchedEffect(Unit) {
        authViewModel.fetchUserProfile()
    }

    Box(
        Modifier
            .background(white)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar with improved alignment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.goback),
                    contentDescription = "Back",
                    modifier = Modifier
                        .clickable { navController.popBackStack() }
                        .size(40.dp),
                )
                Text(
                    text = "My Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyBlue,
                    fontFamily = alifbaFont
                )
                // Empty box for symmetry
                Box(modifier = Modifier.size(40.dp))
            }

            // Just the Lottie animation with no box or border
            LottieAnimation(
                composition = lottieComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(140.dp)
            )

//            Spacer(modifier = Modifier.height(2.dp))
            Spacer(modifier = Modifier.height(-8.dp)) // Add this after the animation
            // Profile Card with black border
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                backgroundColor = cardBackground,
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = cardBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Account Information",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = navyBlue
                        ),
                        fontFamily = alifbaFont,
                    )

                    Divider(color = cardBorder.copy(alpha = 0.5f), thickness = 1.dp)

                    // Parent Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Parent",
                            tint = iconTint,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Parent Name",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = navyBlue.copy(alpha = 0.7f)
                                ),
                                fontFamily = alifbaFont,
                            )
                            Text(
                                text = userProfile?.parentName ?: "Loading...",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textHighlightColor
                                ),
                                fontFamily = alifbaFont,
                            )
                        }
                    }

                    Divider(color = cardBorder.copy(alpha = 0.2f), thickness = 1.dp)

                    // Email
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = iconTint,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Email Address",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = navyBlue.copy(alpha = 0.7f)
                                ),
                                fontFamily = alifbaFont,
                            )
                            Text(
                                text = userProfile?.email ?: "Loading...",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textHighlightColor
                                ),
                                fontFamily = alifbaFont,
                            )
                        }
                    }

                    Divider(color = cardBorder.copy(alpha = 0.2f), thickness = 1.dp)

                    // User ID
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "User ID",
                            tint = iconTint,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Account ID",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = navyBlue.copy(alpha = 0.7f)
                                ),
                                fontFamily = alifbaFont,
                            )
                            Text(
                                text = userProfile?.userId ?: "Loading...",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textHighlightColor
                                ),
                                fontFamily = alifbaFont,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons at the bottom with more space
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CommonButton(
                    onClick = {
                        authViewModel.logout() // Clears user data and signs out
                        navController.navigate("login") {
                            popUpTo("homeScreen") { inclusive = true } // Clear back stack
                        }
                    },
                    buttonText = "Logout",
                    shadowColor = navyBlue,
                    mainColor = lightNavyBlue,
                    textColor = white,
                )

                CommonButton(
                    onClick = { showDeleteConfirmDialog = true },
                    buttonText = "Delete Account",
                    shadowColor = darkRed,
                    mainColor = lightRed,
                    textColor = white,
                )
            }
        }
    }

    // Delete Account Confirmation Dialog with improved styling
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = lightRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Delete Account",
                        fontFamily = alifbaFont,
                        color = navyBlue,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your account? This action cannot be undone. All your data will be permanently removed.",
                    fontFamily = alifbaFont,
                    color = navyBlue,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch { deleteUserAccount(authViewModel, navController) }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = lightRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Delete",
                        color = white,
                        fontFamily = alifbaFont,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = lightNavyBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = white,
                        fontFamily = alifbaFont,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            },
            backgroundColor = white,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Add this extension function to fix the missing Spacer width method
fun deleteUserAccount(authViewModel: AuthViewModel, navController: NavController) {
    // Call the ViewModel method to handle account deletion
    authViewModel.deleteUserAccount(
        onSuccess = {
            // Navigate to login screen after successful deletion
            navController.navigate("login") {
                popUpTo("homeScreen") { inclusive = true }
            }
        },
        onError = { errorMessage ->
            // Handle error (you might want to show a Toast here)
            Log.e("AccountScreen", "Error deleting account: $errorMessage")
        }
    )
}
fun Modifier.width(dp: Number) = padding(horizontal = dp.toInt().dp / 2)